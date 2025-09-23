package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User create(UserRequest request);

    List<User> createBulk(List<UserRequest> requests);

    User update(Long id, UserRequest request);

    void delete(Long id);

    void deletePermanent(Long id);

    User get(Long id);

    Page<User> list(Pageable pageable);
}
