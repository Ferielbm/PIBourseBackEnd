package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Order.Entity.AuditLog;
import tn.esprit.piboursebackend.Order.Repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository repo;

    public void log(String actor, String action, String details) {
        repo.save(AuditLog.builder()
                .actor(actor != null ? actor : "engine")
                .action(action)
                .details(details)
                .build());
    }
}
