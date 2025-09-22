package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserRequest;

import java.util.List;

public interface UserService {
    User create(UserRequest request);

    User update(Long id, UserRequest request);

    void delete(Long id);

    User get(Long id);

    List<User> list();
}
