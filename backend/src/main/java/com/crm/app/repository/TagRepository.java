package com.crm.app.repository;

import com.crm.app.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // ==================== READ ====================
    // Buscar por nombre exacto (case-insensitive)
    Optional<Tag> findByNameIgnoreCase(String name);
    
    // Buscar por nombre parcial (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Buscar por nombre parcial (case-insensitive)
    List<Tag> findByNameContainingIgnoreCase(String name);

    // Listar todas las etiquetas ordenadas por nombre
    List<Tag> findAllByOrderByNameAsc();
}