package com.crm.app.model;

import com.crm.app.model.enums.Channel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Plantilla de mensaje predefinida por el Admin.
 * Permite al equipo enviar mensajes consistentes y rápidos sin redactar desde cero.
 *
 * Reglas de negocio:
 * - Solo el Admin puede crear, editar o eliminar plantillas.
 * - Los Vendedores solo pueden seleccionarlas al redactar un mensaje.
 * - El campo variables define los marcadores dinámicos en formato JSON.
 *   Ejemplo: {"nombre": "string", "empresa": "string"}
 *   En el cuerpo se usan como: "Hola {{nombre}}, gracias por contactar a {{empresa}}."
 * - El canal determina si la plantilla es para WhatsApp o email.
 *   Una plantilla de WhatsApp no puede usarse para email y viceversa.
 */
@Entity
@Table(name = "templates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Canal al que aplica esta plantilla. Inmutable después de crear.
     * WhatsApp y email tienen formatos y restricciones distintas.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    /**
     * Asunto del email. Solo aplica cuando channel = EMAIL.
     * Puede contener variables dinámicas: "Propuesta para {{empresa}}".
     */
    @Column
    private String subject;

    /**
     * Cuerpo del mensaje. Puede contener variables con doble llave: {{nombre}}.
     * El sistema las reemplaza automáticamente al momento de envío.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")  // Cambiar de jsonb a TEXT
    private String variables;

    /**
     * Usuario que creó la plantilla.
     * - Si es ADMIN → plantilla GLOBAL (visible para todos)
     * - Si es VENDEDOR → plantilla PERSONAL (visible solo para él)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
