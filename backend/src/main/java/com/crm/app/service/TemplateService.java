package com.crm.app.service;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.exception.*;
import com.crm.app.mapper.TemplateMapper;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.TemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper; // Inyectar mapper
    private final ObjectMapper objectMapper;

    // ==================== MÉTODOS CON DTO ====================

    public TemplateDTOs.TemplateResponse createTemplateResponse(TemplateDTOs.TemplateRequest request, User currentUser) {
        Template template = createTemplate(request, currentUser);
        return templateMapper.toResponse(template);
    }

    public TemplateDTOs.TemplateResponse updateTemplateResponse(Long id, TemplateDTOs.TemplateRequest request, User currentUser) {
        Template template = updateTemplate(id, request, currentUser);
        return templateMapper.toResponse(template);
    }

    public TemplateDTOs.TemplateResponse getTemplateResponse(Long id, User currentUser) {
        Template template = getTemplate(id, currentUser);
        return templateMapper.toResponse(template);
    }

    public List<TemplateDTOs.TemplateResponse> listTemplatesResponse(User currentUser, Channel channel) {
        List<Template> templates = listTemplates(currentUser, channel);
        return templateMapper.toResponseList(templates);
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Template createTemplate(TemplateDTOs.TemplateRequest request, User currentUser) {
        // Validar nombre único para este contexto
        if (currentUser.getRole() == Role.ADMIN) {
            if (templateRepository.existsByNameAndCreatedByRole(request.name(), Role.ADMIN)) {
                throw new DuplicateResourceException("plantilla global", "nombre", request.name());
            }
        } else {
            if (templateRepository.existsByNameAndCreatedBy(request.name(), currentUser)) {
                throw new DuplicateResourceException("plantilla personal", "nombre", request.name());
            }
        }

        validateVariables(request.body(), request.variables());

        Template template = Template.builder()
                .name(request.name())
                .channel(request.channel())
                .subject(request.subject())
                .body(request.body())
                .variables(toJson(request.variables()))
                .createdBy(currentUser)
                .build();

        log.info("{} creó plantilla: {}", currentUser.getEmail(), request.name());
        return templateRepository.save(template);
    }

    public Template updateTemplate(Long id, TemplateDTOs.TemplateRequest request, User currentUser) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));

        // Solo el creador puede modificar
        if (!template.getCreatedBy().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("No puedes modificar esta plantilla");
        }

        validateVariables(request.body(), request.variables());

        template.setName(request.name());
        template.setChannel(request.channel());
        template.setSubject(request.subject());
        template.setBody(request.body());
        template.setVariables(toJson(request.variables()));

        log.info("{} actualizó plantilla: {}", currentUser.getEmail(), request.name());
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id, User currentUser) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));

        if (!template.getCreatedBy().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("No puedes eliminar esta plantilla");
        }

        templateRepository.delete(template);
        log.info("{} eliminó plantilla: {}", currentUser.getEmail(), template.getName());
    }

    public List<Template> listTemplates(User currentUser, Channel channel) {
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin ve todas
            return channel != null ? templateRepository.findByChannel(channel) : templateRepository.findAll();
        } else {
            // Vendedor ve: globales (de ADMIN) + sus personales
            return channel != null
                    ? templateRepository.findByChannelAndGlobalOrUser(channel, currentUser)
                    : templateRepository.findGlobalAndUserTemplates(currentUser);
        }
    }

    public Template getTemplate(Long id, User currentUser) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isGlobal = template.getCreatedBy().getRole() == Role.ADMIN;
        boolean isOwner = template.getCreatedBy().getId().equals(currentUser.getId());

        if (isAdmin || isGlobal || isOwner) {
            return template;
        }

        throw new UnauthorizedAccessException("No tienes acceso a esta plantilla");
    }

    public String renderTemplate(Template template, Map<String, String> values) {
        String rendered = template.getBody();
        Map<String, String> vars = fromJson(template.getVariables());

        for (String var : vars.keySet()) {
            String value = values != null ? values.get(var) : null;
            if (value == null) {
                throw new BusinessRuleViolationException("Falta variable: {{" + var + "}}");
            }
            rendered = rendered.replace("{{" + var + "}}", value);
        }
        return rendered;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void validateVariables(String body, Map<String, String> declared) {
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)}}");
        Matcher matcher = pattern.matcher(body);
        Set<String> bodyVars = new HashSet<>();
        while (matcher.find()) bodyVars.add(matcher.group(1));

        for (String var : bodyVars) {
            if (declared == null || !declared.containsKey(var)) {
                throw new BusinessRuleViolationException("Variable '{{" + var + "}}' no declarada");
            }
        }
    }

    private String toJson(Map<String, String> map) {
        try {
            return map == null ? "{}" : objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, String> fromJson(String json) {
        try {
            return json == null ? Map.of() : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}