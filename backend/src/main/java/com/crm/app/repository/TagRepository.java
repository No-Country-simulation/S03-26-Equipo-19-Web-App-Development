package com.crm.app.repository;

import com.crm.app.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    // Búsqueda por nombre parcial para autocompletado en la UI
    List<Tag> findByNameContainingIgnoreCase(String name);
}
