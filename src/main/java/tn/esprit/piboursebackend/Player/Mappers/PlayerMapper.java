package tn.esprit.piboursebackend.Player.Mappers;

import tn.esprit.piboursebackend.Player.DTOs.PlayerCreateDTO;
import tn.esprit.piboursebackend.Player.DTOs.PlayerDTO;
import tn.esprit.piboursebackend.Player.DTOs.PlayerUpdateDTO;
import tn.esprit.piboursebackend.Player.Entities.Player;

public class PlayerMapper {

    public static PlayerDTO toDTO(Player player) {
        if (player == null) {
            return null;
        }
        
        return PlayerDTO.builder()
                .id(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole())
                .build();
    }

    public static Player toEntity(PlayerCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Player.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword()) // In production, this should be encrypted
                .role(dto.getRole())
                .build();
    }

    public static void updateEntityFromDTO(PlayerUpdateDTO dto, Player player) {
        if (dto.getUsername() != null) {
            player.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            player.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            player.setPassword(dto.getPassword()); // In production, this should be encrypted
        }
        if (dto.getRole() != null) {
            player.setRole(dto.getRole());
        }
    }
}

