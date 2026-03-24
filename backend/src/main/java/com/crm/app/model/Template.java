package com.crm.app.model;

import com.crm.app.model.enums.Channel;
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

    /**
     * Definición de variables dinámicas en formato JSON.
     * Ejemplo: {"nombre": "string", "empresa": "string", "fecha": "date"}
     * Se usa para validar que todas las variables estén completas antes de enviar.
     */
    @Column(columnDefinition = "jsonb")
    private String variables;

    /**
     * Usuario Admin que creó la plantilla. Sirve para auditoría.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
