package tn.esprit.piboursebackend.Player.Services;

import tn.esprit.piboursebackend.Player.DTOs.TransactionCreateDTO;
import tn.esprit.piboursebackend.Player.DTOs.TransactionDTO;

import java.util.List;

public interface ITransactionService {
    List<TransactionDTO> getAllTransactions();
    TransactionDTO getTransactionById(Long id);
    TransactionDTO createTransaction(TransactionCreateDTO transactionCreateDTO);
    void deleteTransaction(Long id);
    List<TransactionDTO> getTransactionsByPlayerId(Long playerId);
}
