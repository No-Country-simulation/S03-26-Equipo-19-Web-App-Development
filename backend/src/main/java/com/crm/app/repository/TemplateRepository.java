package com.crm.app.repository;

import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndCreatedByRole(String name, Role role);

    boolean existsByNameAndCreatedBy(String name, User createdBy);

    List<Template> findByChannel(Channel channel);

    @Query("SELECT t FROM Template t WHERE t.createdBy.role = 'ADMIN' OR t.createdBy = :user")
    List<Template> findGlobalAndUserTemplates(@Param("user") User user);

    @Query("SELECT t FROM Template t WHERE t.channel = :channel AND (t.createdBy.role = 'ADMIN' OR t.createdBy = :user)")
    List<Template> findByChannelAndGlobalOrUser(@Param("channel") Channel channel, @Param("user") User user);
}