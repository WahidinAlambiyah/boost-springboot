package com.example.boost.repository;

import com.example.boost.domain.entity.OutboxEvent;
import com.example.boost.domain.entity.OutboxEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select oe from OutboxEvent oe
            where oe.status = :status
            order by oe.createdAt asc
            """)
    List<OutboxEvent> findForPublishing(@Param("status") OutboxEventStatus status, Pageable pageable);
}
