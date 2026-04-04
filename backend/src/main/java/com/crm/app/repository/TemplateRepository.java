package com.crm.app.repository;

import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    boolean existsByName(String name);

    Optional<Template> findByName(String name);
    
    List<Template> findByChannel(Channel channel);
}
