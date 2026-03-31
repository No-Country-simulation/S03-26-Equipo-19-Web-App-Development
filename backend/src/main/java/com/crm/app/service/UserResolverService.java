package com.crm.app.service;

import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.TokenExpiredException;
import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserResolverService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        log.debug("🔍 Buscando usuario por email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));
    }

    public User getDefaultAdmin() {
        log.debug("🔍 Buscando administrador por defecto");
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new ResourceNotFoundException("No hay ningún administrador activo en el sistema");
        }
        return admins.getFirst();
    }

    public boolean existsUserByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}