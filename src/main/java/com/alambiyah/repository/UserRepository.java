package com.alambiyah.repository;

import com.alambiyah.app.Role;
import com.alambiyah.app.User;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Query("select u.roles from User u where u.id = :userId")
    Set<Role> findRolesByUserId(@Param("userId") UUID userId);
}
