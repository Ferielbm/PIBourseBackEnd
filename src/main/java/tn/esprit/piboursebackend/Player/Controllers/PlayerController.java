package tn.esprit.piboursebackend.Player.Controllers;

import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Services.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Player Management", description = "API pour la gestion des joueurs")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les joueurs", description = "Retourne la liste de tous les joueurs")
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un joueur par ID", description = "Retourne un joueur spécifique par son ID")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Récupérer un joueur par email", description = "Retourne un joueur spécifique par son email")
    public ResponseEntity<Player> getPlayerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(playerService.getPlayerByEmail(email));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau joueur", description = "Crée un nouveau joueur dans le système")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player savedPlayer = playerService.createPlayer(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlayer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un joueur", description = "Met à jour les informations d'un joueur existant")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        return ResponseEntity.ok(playerService.updatePlayer(id, player));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un joueur", description = "Supprime un joueur du système")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
