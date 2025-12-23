package tn.esprit.piboursebackend.Marche.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Marche.Entity.AlternativeTrade;
import tn.esprit.piboursebackend.Marche.Entity.TimeTravelResult;
import tn.esprit.piboursebackend.Marche.Entity.TimeTravelSession;
import tn.esprit.piboursebackend.Marche.Service.TimeTravelService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/time-travel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimeTravelController {

    private final TimeTravelService timeTravelService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTimeTravelSession(
            @RequestParam String playerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime rewindToDate) {

        try {
            TimeTravelSession session = timeTravelService.startTimeTravelSession(playerId, rewindToDate);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "🕰️ Time travel session started successfully",
                    "sessionId", session.getSessionId(),
                    "rewindToDate", session.getRewindToDate(),
                    "originalPortfolioValue", session.getOriginalPortfolioValue()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to start time travel session", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{sessionId}/trade")
    public ResponseEntity<Map<String, Object>> executeAlternativeTrade(
            @PathVariable String sessionId,
            @RequestBody AlternativeTrade tradeRequest) {

        try {
            AlternativeTrade trade = timeTravelService.executeAlternativeTrade(sessionId, tradeRequest);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "📊 Alternative trade executed",
                    "tradeId", trade.getId(),
                    "symbol", trade.getSymbol(),
                    "action", trade.getAction(),
                    "executionPrice", trade.getExecutionPrice()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to execute alternative trade", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<Map<String, Object>> completeTimeTravelSession(@PathVariable String sessionId) {
        try {
            TimeTravelResult result = timeTravelService.completeTimeTravelSession(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "🎯 Time travel analysis completed",
                    "results", result
            ));
        } catch (Exception e) {
            log.error("❌ Failed to complete time travel session", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{sessionId}/preview")
    public ResponseEntity<Map<String, Object>> previewResults(@PathVariable String sessionId) {
        try {
            TimeTravelResult preview = timeTravelService.previewResults(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "preview", preview
            ));
        } catch (Exception e) {
            log.error("❌ Failed to preview time travel results", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/player/{playerId}/sessions")
    public ResponseEntity<List<TimeTravelSession>> getPlayerSessions(@PathVariable String playerId) {
        try {
            List<TimeTravelSession> sessions = timeTravelService.getPlayerSessions(playerId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Failed to get player sessions", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "🟢 Time Travel Trading is operational",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}