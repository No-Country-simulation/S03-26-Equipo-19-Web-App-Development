package com.crm.app.repository;

import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    // Todas las plantillas de un canal específico — para mostrar en el selector al redactar
    List<Template> findByChannel(Channel channel);

    // Plantillas creadas por un Admin específico — para auditoría
    List<Template> findByCreatedBy(User createdBy);

    boolean existsByName(String name);

    // Búsqueda por nombre parcial para autocompletado
    List<Template> findByNameContainingIgnoreCase(String name);

    // Plantillas de un canal ordenadas por nombre — para el selector de la UI
    List<Template> findByChannelOrderByNameAsc(Channel channel);
}
