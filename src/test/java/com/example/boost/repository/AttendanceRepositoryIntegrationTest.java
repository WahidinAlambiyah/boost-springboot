package com.example.boost.repository;

import com.example.boost.domain.dto.AttendanceUpsertRequest;
import com.example.boost.scheduling.infrastructure.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-attendance-repo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.liquibase.enabled=false"
})
class AttendanceRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUpSchema() {
        jdbcTemplate.execute("create schema if not exists fastworks_springboot");
        jdbcTemplate.execute("drop table if exists fastworks_springboot.attendance_records");
        jdbcTemplate.execute("drop table if exists fastworks_springboot.class_sessions");
        jdbcTemplate.execute("drop table if exists fastworks_springboot.students");

        jdbcTemplate.execute("""
                create table fastworks_springboot.students (
                  id uuid primary key,
                  full_name varchar(120),
                  deleted_at timestamp with time zone
                )
                """);

        jdbcTemplate.execute("""
                create table fastworks_springboot.class_sessions (
                  id uuid primary key,
                  deleted_at timestamp with time zone
                )
                """);

        jdbcTemplate.execute("""
                create table fastworks_springboot.attendance_records (
                  id uuid primary key default random_uuid(),
                  class_session_id uuid not null,
                  student_id uuid not null,
                  attendance_status varchar(20) not null,
                  check_in_at timestamp with time zone,
                  remarks varchar(500),
                  created_at timestamp with time zone not null default current_timestamp,
                  updated_at timestamp with time zone not null default current_timestamp,
                  constraint uk_attendance_records_session_student unique (class_session_id, student_id),
                  constraint fk_attendance_records_session foreign key (class_session_id) references fastworks_springboot.class_sessions(id),
                  constraint fk_attendance_records_student foreign key (student_id) references fastworks_springboot.students(id)
                )
                """);
    }

    @Test
    void upsertShouldKeepSingleRowForSameSessionAndStudent() {
        UUID classSessionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        jdbcTemplate.update("insert into fastworks_springboot.class_sessions(id, deleted_at) values (?, ?)", classSessionId, null);
        jdbcTemplate.update("insert into fastworks_springboot.students(id, full_name, deleted_at) values (?, ?, ?)", studentId, "Student A", null);

        attendanceRepository.upsertAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", OffsetDateTime.parse("2026-01-01T08:00:00Z"), "first post"));
        attendanceRepository.upsertAttendance(classSessionId, studentId, new AttendanceUpsertRequest("LATE", OffsetDateTime.parse("2026-01-01T08:15:00Z"), "second post/put"));

        Integer rowCount = jdbcTemplate.queryForObject(
                "select count(*) from fastworks_springboot.attendance_records where class_session_id = ? and student_id = ?",
                Integer.class,
                classSessionId,
                studentId
        );
        String latestStatus = jdbcTemplate.queryForObject(
                "select attendance_status from fastworks_springboot.attendance_records where class_session_id = ? and student_id = ?",
                String.class,
                classSessionId,
                studentId
        );

        assertThat(rowCount).isEqualTo(1);
        assertThat(latestStatus).isEqualTo("LATE");
    }
}
