package com.crm.app.service;

import com.crm.app.exception.DuplicateResourceException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.Tag;
import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // ==================== READ ====================

    public List<Tag> getAll(User user) {
        // Ambos roles pueden ver
        return tagRepository.findAllByOrderByNameAsc();
    }

    public Tag getById(Long id, User user) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrada con id: " + id));
    }

    public List<Tag> search(String name, User user) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }

    // ==================== CREATE ====================
    @Transactional
    public Tag create(String name, User user) {
        validateAdmin(user);

        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Ya existe una etiqueta con nombre: " + name);
        }

        Tag tag = Tag.builder()
                .name(name.trim())
                .build();

        return tagRepository.save(tag);
    }

    // ==================== UPDATE ====================
    @Transactional
    public Tag update(Long id, String name, User user) {
        validateAdmin(user);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrada con id: " + id));

        if (tagRepository.existsByNameIgnoreCase(name)
                && !tag.getName().equalsIgnoreCase(name)) {
            throw new DuplicateResourceException("Ya existe una etiqueta con nombre: " + name);
        }

        tag.setName(name.trim());

        return tagRepository.save(tag);
    }

    // ==================== DELETE ====================
    @Transactional
    public void delete(Long id, User user) {
        validateAdmin(user);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag no encontrada con id: " + id));

        tagRepository.delete(tag);
    }

    // ==================== PRIVATE ====================

    private void validateAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("No tienes permisos para realizar esta acción");
        }
    }
}