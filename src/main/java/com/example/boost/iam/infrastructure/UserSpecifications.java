package com.example.boost.iam.infrastructure;

import com.example.boost.domain.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<User> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Specification.where(null);
        }
        String likeKeyword = "%" + keyword.toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeKeyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeKeyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword)
        );
    }
}
