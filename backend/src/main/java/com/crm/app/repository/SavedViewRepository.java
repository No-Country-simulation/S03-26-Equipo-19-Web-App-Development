package com.crm.app.repository;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedViewRepository extends JpaRepository<SavedView, Long> {

    // Vistas privadas de un usuario específico
    List<SavedView> findByUserAndGlobalFalse(User user);

    // Todas las vistas globales — visibles para cualquier usuario
    List<SavedView> findByGlobalTrue();

    // Lo que ve un usuario al abrir el selector de vistas:
    // sus vistas privadas + todas las globales
    @Query("SELECT v FROM SavedView v WHERE v.user = :user OR v.global = true " +
            "ORDER BY v.global DESC, v.name ASC")
    List<SavedView> findAccessibleByUser(@Param("user") User user);

    // Vistas accesibles filtradas por entidad (contacts, tasks)
    @Query("SELECT v FROM SavedView v WHERE (v.user = :user OR v.global = true) " +
            "AND v.entity = :entity ORDER BY v.global DESC, v.name ASC")
    List<SavedView> findAccessibleByUserAndEntity(
            @Param("user") User user,
            @Param("entity") EntityType entity
    );

    // Verificar acceso antes de editar o eliminar:
    Optional<SavedView> findByIdAndUser(Long id, User user);

    boolean existsByNameAndUser(String name, User user);
}