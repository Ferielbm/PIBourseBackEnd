package tn.esprit.piboursebackend.Player.Mappers;

import tn.esprit.piboursebackend.Player.DTOs.TransactionCreateDTO;
import tn.esprit.piboursebackend.Player.DTOs.TransactionDTO;
import tn.esprit.piboursebackend.Player.Entities.Transaction;

public class TransactionMapper {

    public static TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .playerId(transaction.getPlayer() != null ? transaction.getPlayer().getId() : null)
                .playerUsername(transaction.getPlayer() != null ? transaction.getPlayer().getUsername() : null)
                .build();
    }

    public static Transaction toEntity(TransactionCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Transaction.builder()
                .type(dto.getType())
                .amount(dto.getAmount())
                .build();
    }
}

