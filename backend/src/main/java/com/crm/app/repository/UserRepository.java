package com.crm.app.repository;

import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    long countByActiveTrue();

    long countByCreatedAtAfter(LocalDateTime date);

    // Contar usuarios totales creados hasta una fecha
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt <= :endDate")
    long countTotalWithDate(@Param("endDate") LocalDateTime endDate);

    // Contar usuarios activos creados hasta una fecha
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.createdAt <= :endDate")
    long countActiveWithDate(@Param("endDate") LocalDateTime endDate);
}
