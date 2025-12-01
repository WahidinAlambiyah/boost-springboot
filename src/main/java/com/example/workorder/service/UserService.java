package com.example.workorder.service;

import com.example.workorder.domain.Division;
import com.example.workorder.domain.Role;
import com.example.workorder.domain.UserAccount;
import com.example.workorder.dto.AccessResponse;
import com.example.workorder.dto.RoleResponse;
import com.example.workorder.dto.UserRequest;
import com.example.workorder.dto.UserResponse;
import com.example.workorder.exception.NotFoundException;
import com.example.workorder.repository.DivisionRepository;
import com.example.workorder.repository.RoleRepository;
import com.example.workorder.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final DivisionRepository divisionRepository;
    private final RoleRepository roleRepository;

    public UserService(UserAccountRepository userAccountRepository,
                       DivisionRepository divisionRepository,
                       RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.divisionRepository = divisionRepository;
        this.roleRepository = roleRepository;
    }

    public UserResponse createUser(UserRequest request) {
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        if (request.getDivisionId() != null) {
            Division division = divisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new NotFoundException("Division not found with id " + request.getDivisionId()));
            user.setDivision(division);
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new NotFoundException("One or more role ids do not exist");
            }
        }
        user.setRoles(roles);

        UserAccount saved = userAccountRepository.save(user);
        return toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userAccountRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserAccount getUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
    }

    public UserResponse getUserResponse(Long id) {
        return toResponse(getUser(id));
    }

    private UserResponse toResponse(UserAccount user) {
        List<RoleResponse> roleResponses = user.getRoles().stream()
                .map(role -> new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.getAccesses().stream()
                        .map(access -> new AccessResponse(access.getId(), access.getCode(), access.getDescription()))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
        Long divisionId = user.getDivision() != null ? user.getDivision().getId() : null;
        String divisionName = user.getDivision() != null ? user.getDivision().getName() : null;
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), divisionId, divisionName, roleResponses);
    }
}
