package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User create(UserRequest request);

    User update(Long id, UserRequest request);

    void delete(Long id);

    User get(Long id);

    Page<User> list(Pageable pageable);
}
