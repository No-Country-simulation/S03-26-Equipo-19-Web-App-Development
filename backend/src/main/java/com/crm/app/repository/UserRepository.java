package com.crm.app.repository;

import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Todos los vendedores activos — usado por el Admin para el CRUD
    List<User> findByRoleAndActiveTrue(Role role);

    // Todos los usuarios activos de cualquier rol
    List<User> findByActiveTrue();

    List<User> findByRole(Role role);
}
