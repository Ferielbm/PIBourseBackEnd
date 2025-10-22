package tn.esprit.piboursebackend.Player.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db-connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            result.put("connected", true);
            result.put("autoCommit", conn.getAutoCommit());
            result.put("catalog", conn.getCatalog());
            result.put("url", conn.getMetaData().getURL());
            
            logger.info("Database connection successful: {}", conn.getMetaData().getURL());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Database connection failed", e);
            result.put("connected", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/insert-direct")
    public ResponseEntity<Map<String, Object>> testDirectInsert() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            logger.info("AutoCommit before insert: {}", conn.getAutoCommit());
            
            String sql = "INSERT INTO player (username, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, "test_direct");
            pstmt.setString(2, "test@direct.com");
            pstmt.setString(3, "password");
            pstmt.setString(4, "ROLE_ADMIN");
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (!conn.getAutoCommit()) {
                conn.commit();
                logger.info("Manual commit executed");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            Long generatedId = null;
            if (rs.next()) {
                generatedId = rs.getLong(1);
            }
            
            result.put("success", true);
            result.put("rowsAffected", rowsAffected);
            result.put("generatedId", generatedId);
            result.put("autoCommit", conn.getAutoCommit());
            
            logger.info("Direct insert successful. ID: {}", generatedId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Direct insert failed", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/count-players")
    public ResponseEntity<Map<String, Object>> countPlayers() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM player");
            
            if (rs.next()) {
                result.put("count", rs.getInt("count"));
            }
            
            logger.info("Player count retrieved successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Count players failed", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}

