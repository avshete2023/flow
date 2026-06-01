package com.flow.audit.domain.repository;

import com.flow.audit.domain.entity.AuditEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("""
            SELECT a
            FROM AuditEvent a
            WHERE (:actorId IS NULL OR a.actorId = :actorId)
              AND (:eventType IS NULL OR a.eventType = :eventType)
              AND (:resourceType IS NULL OR a.resourceType = :resourceType)
              AND (:startDate IS NULL OR a.occurredAt >= :startDate)
              AND (:endDate IS NULL OR a.occurredAt <= :endDate)
            """)
    Page<AuditEvent> findByFilters(
            @Param("actorId") UUID actorId,
            @Param("eventType") String eventType,
            @Param("resourceType") String resourceType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

