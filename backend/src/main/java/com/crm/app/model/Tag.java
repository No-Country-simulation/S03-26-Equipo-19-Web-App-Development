package com.crm.app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Etiqueta de clasificación libre definida por el Admin.
 * Se usa para segmentar contactos por industria, prioridad, origen, etc.
 * Relación muchos-a-muchos con Contact a través de ContactTag.
 * Solo el Admin puede crear, editar o eliminar etiquetas.
 */
@Entity
@Table(name = "tags")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre visible de la etiqueta. Ej: "Alta prioridad", "Fintech", "Referido".
     */

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    /**
     * Color en formato HEX para identificación visual en la UI. Ej: "#FF5733".
     */
    @Column(nullable = false, length = 7)
    @Builder.Default
    private String color = "#6366F1";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
