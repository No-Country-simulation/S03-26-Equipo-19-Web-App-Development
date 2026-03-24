package com.crm.app.repository;

import com.crm.app.model.Contact;
import com.crm.app.model.User;
import com.crm.app.model.enums.FunnelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JpaSpecificationExecutor permite construir queries dinámicas en tiempo de ejecución.
 * Es necesario para el módulo de vistas guardadas, donde los filtros son configurables.
 */
public interface ContactRepository extends JpaRepository<Contact, Long>,
        JpaSpecificationExecutor<Contact> {

    // Un Vendedor solo puede ver sus propios contactos
    List<Contact> findByOwner(User owner);

    // Filtro por estado del funnel para un vendedor específico
    List<Contact> findByOwnerAndFunnelStatus(User owner, FunnelStatus funnelStatus);

    // Todos los contactos en un estado del funnel — usado por el Admin
    List<Contact> findByFunnelStatus(FunnelStatus funnelStatus);

    // Verificar si ya existe un contacto con ese email para ese vendedor
    boolean existsByEmailAndOwner(String email, User owner);

    // Contactos que tienen una etiqueta específica — para segmentación
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.id = :tagId")
    List<Contact> findByTagId(@Param("tagId") Long tagId);

    // Contactos de un vendedor que tienen una etiqueta específica
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE c.owner = :owner AND t.id = :tagId")
    List<Contact> findByOwnerAndTagId(@Param("owner") User owner, @Param("tagId") Long tagId);

    // Cantidad de contactos por estado del funnel — usado por métricas
    @Query("SELECT c.funnelStatus, COUNT(c) FROM Contact c GROUP BY c.funnelStatus")
    List<Object[]> countByFunnelStatus();

    // Cantidad de contactos por estado del funnel para un vendedor — métricas propias
    @Query("SELECT c.funnelStatus, COUNT(c) FROM Contact c WHERE c.owner = :owner GROUP BY c.funnelStatus")
    List<Object[]> countByFunnelStatusAndOwner(@Param("owner") User owner);

    // Búsqueda por nombre o email para la barra de búsqueda de la UI
    @Query("SELECT c FROM Contact c WHERE c.owner = :owner AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Contact> searchByOwner(@Param("owner") User owner, @Param("query") String query);

    // Búsqueda global — solo Admin
    @Query("SELECT c FROM Contact c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Contact> searchAll(@Param("query") String query);

    // Contacto por id restringido al owner — evita acceso cruzado entre vendedores
    Optional<Contact> findByIdAndOwner(Long id, User owner);
}
