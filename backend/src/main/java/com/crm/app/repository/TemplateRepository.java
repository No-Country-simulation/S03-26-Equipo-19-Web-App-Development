package com.crm.app.repository;

import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    // ==================== BÚSQUEDAS BÁSICAS ====================

    Optional<Template> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndCreatedByRole(String name, Role role);

    boolean existsByNameAndCreatedBy(String name, User createdBy);

    List<Template> findByChannel(Channel channel);

    Optional<Template> findByNameAndChannel(String name, Channel channel);

    Optional<Template> findByNameAndChannelAndCreatedByRole(String name, Channel channel, Role role);

    // ==================== LISTADOS POR ROL ====================

    @Query("SELECT t FROM Template t WHERE t.createdBy.role = 'ADMIN' OR t.createdBy = :user")
    List<Template> findGlobalAndUserTemplates(@Param("user") User user);

    @Query("SELECT t FROM Template t WHERE t.channel = :channel AND (t.createdBy.role = 'ADMIN' OR t.createdBy = :user)")
    List<Template> findByChannelAndGlobalOrUser(@Param("channel") Channel channel, @Param("user") User user);

    // ==================== CONTAR PLANTILLAS (SIN FILTRO DE FECHA) ====================

    @Query("SELECT COUNT(t) FROM Template t")
    long countAllTemplates();

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy = :user")
    long countByCreatedBy(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy.role = :role")
    long countByCreatedByRole(@Param("role") Role role);

    // ==================== CONTAR PLANTILLAS POR FECHA (HASTA UNA FECHA) ====================

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdAt <= :endDate")
    long countByCreatedAtBefore(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy = :user AND t.createdAt <= :endDate")
    long countByCreatedByAndCreatedAtBefore(@Param("user") User user,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy.role = :role AND t.createdAt <= :endDate")
    long countByCreatedByRoleAndCreatedAtBefore(@Param("role") Role role,
                                                @Param("endDate") LocalDateTime endDate);

    // ==================== CONTAR PLANTILLAS POR PERÍODO ====================

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy = :user AND t.createdAt BETWEEN :start AND :end")
    long countByCreatedByAndCreatedAtBetween(@Param("user") User user,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.createdBy.role = :role AND t.createdAt BETWEEN :start AND :end")
    long countByCreatedByRoleAndCreatedAtBetween(@Param("role") Role role,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    // ==================== CONTAR PLANTILLAS CREADAS HOY ====================

    default long countCreatedToday(LocalDateTime start, LocalDateTime end) {
        return countByCreatedAtBetween(start, end);
    }

    default long countByCreatedByAndCreatedToday(User user, LocalDateTime start, LocalDateTime end) {
        return countByCreatedByAndCreatedAtBetween(user, start, end);
    }

    default long countByCreatedByRoleAndCreatedToday(Role role, LocalDateTime start, LocalDateTime end) {
        return countByCreatedByRoleAndCreatedAtBetween(role, start, end);
    }

    // ==================== TENDENCIAS POR MES ====================

    @Query("SELECT FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt), COUNT(t) FROM Template t " +
            "WHERE t.createdAt >= :startDate GROUP BY FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt)")
    List<Object[]> countByMonth(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt), COUNT(t) FROM Template t " +
            "WHERE t.createdBy = :user AND t.createdAt >= :startDate " +
            "GROUP BY FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt)")
    List<Object[]> countByMonthAndUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
}