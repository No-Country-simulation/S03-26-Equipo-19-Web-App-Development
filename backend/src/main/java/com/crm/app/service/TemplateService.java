package com.crm.app.service;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.exception.*;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== ADMIN ====================

    public Template createTemplate(TemplateDTOs.TemplateRequest request, User admin) {
        if (admin.getRole() != Role.ADMIN) throw new UnauthorizedAccessException("Solo ADMIN puede crear plantillas");
        if (templateRepository.existsByName(request.name())) throw new DuplicateResourceException("plantilla", "nombre", request.name());

        validateVariables(request.body(), request.variables());

        Template template = Template.builder()
                .name(request.name())
                .channel(request.channel())
                .subject(request.subject())
                .body(request.body())
                .variables(toJson(request.variables()))
                .createdBy(admin)
                .build();

        log.info("Admin {} creó plantilla: {}", admin.getEmail(), request.name());
        return templateRepository.save(template);
    }

    public Template updateTemplate(Long id, TemplateDTOs.TemplateRequest request, User admin) {
        if (admin.getRole() != Role.ADMIN) throw new UnauthorizedAccessException("Solo ADMIN puede modificar plantillas");

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));

        validateVariables(request.body(), request.variables());

        template.setName(request.name());
        template.setChannel(request.channel());
        template.setSubject(request.subject());
        template.setBody(request.body());
        template.setVariables(toJson(request.variables()));

        log.info("Admin {} actualizó plantilla: {}", admin.getEmail(), request.name());
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id, User admin) {
        if (admin.getRole() != Role.ADMIN) throw new UnauthorizedAccessException("Solo ADMIN puede eliminar plantillas");

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));

        templateRepository.delete(template);
        log.info("Admin {} eliminó plantilla: {}", admin.getEmail(), template.getName());
    }

    // ==================== VENDEDOR (solo lectura) ====================

    public List<Template> listTemplates(Channel channel) {
        return channel != null ? templateRepository.findByChannel(channel) : templateRepository.findAll();
    }

    public Template getTemplate(Long id) {
        return templateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plantilla", id));
    }

    // ==================== RENDERIZADO ====================

    public String renderTemplate(Template template, Map<String, String> values) {
        String rendered = template.getBody();
        Map<String, String> vars = fromJson(template.getVariables());

        for (String var : vars.keySet()) {
            String value = values != null ? values.get(var) : null;
            if (value == null) throw new BusinessRuleViolationException("Falta variable: {{" + var + "}}");
            rendered = rendered.replace("{{" + var + "}}", value);
        }
        return rendered;
    }

    // ==================== VALIDACIÓN ====================

    private void validateVariables(String body, Map<String, String> declared) {
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
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
        try { return map == null ? "{}" : objectMapper.writeValueAsString(map); }
        catch (Exception e) { return "{}"; }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> fromJson(String json) {
        try { return json == null ? Map.of() : objectMapper.readValue(json, Map.class); }
        catch (Exception e) { return Map.of(); }
    }
}