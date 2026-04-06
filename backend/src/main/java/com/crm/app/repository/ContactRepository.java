package com.crm.app.repository;

import com.crm.app.model.Contact;
import com.crm.app.model.User;
import com.crm.app.model.enums.FunnelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JpaSpecificationExecutor permite construir queries dinámicas en tiempo de ejecución.
 * Es necesario para el módulo de vistas guardadas, donde los filtros son configurables.
 */
public interface ContactRepository extends JpaRepository<Contact, Long>,
        JpaSpecificationExecutor<Contact> {

    // Métodos básicos de acceso por owner
    List<Contact> findByOwner(User owner);
    Optional<Contact> findByIdAndOwner(Long id, User owner);

    // Métodos para evitar duplicados
    boolean existsByEmailAndOwner(String email, User owner);
    boolean existsByPhoneAndOwner(String phone, User owner);

    // Búsqueda por identificadores externos (webhooks)
    Optional<Contact> findByPhone(String phone);
    Optional<Contact> findByEmail(String email);

    // Filtros para segmentación
    List<Contact> findByOwnerAndFunnelStatus(User owner, FunnelStatus funnelStatus);
    List<Contact> findByFunnelStatus(FunnelStatus funnelStatus);

    // Búsqueda con texto
    @Query("SELECT c FROM Contact c WHERE c.owner = :owner AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Contact> searchByOwner(@Param("owner") User owner, @Param("query") String query);

    // Métricas
    // ✅ MÉTRICAS
    @Query("SELECT c.funnelStatus, COUNT(c) FROM Contact c GROUP BY c.funnelStatus")
    List<Object[]> countByFunnelStatus();

    @Query("SELECT c.funnelStatus, COUNT(c) FROM Contact c WHERE c.owner = :owner GROUP BY c.funnelStatus")
    List<Object[]> countByFunnelStatusAndOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.funnelStatus IN :activeStatuses")
    long countByFunnelStatusIn(@Param("activeStatuses") List<FunnelStatus> activeStatuses);

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.createdAt BETWEEN :start AND :end")
    long countNewContactsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
