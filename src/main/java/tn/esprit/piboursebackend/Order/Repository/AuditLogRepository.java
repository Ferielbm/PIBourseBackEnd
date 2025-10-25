package tn.esprit.piboursebackend.Order.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Order.Entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }
