package com.crm.app.service;

import com.crm.app.dto.UserDTOs;
import com.crm.app.exception.ConflictException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /** Returns all active salespersons */
    public List<UserDTOs.UserResponse> listSalespersons() {
        return repository.findByRoleAndActiveTrue(Role.SALESPERSON)
                .stream()
                .map(UserDTOs.UserResponse::from)
                .toList();
    }

    /** Returns a salesperson by id — throws 404 if not found or not a salesperson */
    public UserDTOs.UserResponse getSalesperson(Long id) {
        User user = repository.findById(id)
                .filter(u -> u.getRole() == Role.SALESPERSON)
                .orElseThrow(() -> new ResourceNotFoundException("Salesperson not found with id: " + id));
        return UserDTOs.UserResponse.from(user);
    }

    /** Creates a new salesperson. Only Admin can invoke this. */
    @Transactional
    public UserDTOs.UserResponse createSalesperson(UserDTOs.CreateSalespersonRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("A user with email " + request.email() + " already exists");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.SALESPERSON)
                .active(true)
                .build();
        return UserDTOs.UserResponse.from(repository.save(user));
    }

    /** Updates the name or password of an existing salesperson */
    @Transactional
    public UserDTOs.UserResponse updateSalesperson(Long id, UserDTOs.UpdateSalespersonRequest request) {
        User user = repository.findById(id)
                .filter(u -> u.getRole() == Role.SALESPERSON)
                .orElseThrow(() -> new ResourceNotFoundException("Salesperson not found with id: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserDTOs.UserResponse.from(repository.save(user));
    }

    /**
     * Soft delete — marks the salesperson as inactive instead of deleting the record.
     * Preserves referential integrity with assigned contacts.
     */
    @Transactional
    public void deactivateSalesperson(Long id) {
        User user = repository.findById(id)
                .filter(u -> u.getRole() == Role.SALESPERSON)
                .orElseThrow(() -> new ResourceNotFoundException("Salesperson not found with id: " + id));
        user.setActive(false);
        repository.save(user);
    }
}