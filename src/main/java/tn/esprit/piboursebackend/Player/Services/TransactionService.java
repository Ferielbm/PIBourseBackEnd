package tn.esprit.piboursebackend.Player.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Player.DTOs.TransactionCreateDTO;
import tn.esprit.piboursebackend.Player.DTOs.TransactionDTO;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Transaction;
import tn.esprit.piboursebackend.Player.Exceptions.ResourceNotFoundException;
import tn.esprit.piboursebackend.Player.Mappers.TransactionMapper;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Repositories.TransactionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService implements ITransactionService {
    
    private final TransactionRepository transactionRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        log.info("Fetching transaction with id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return TransactionMapper.toDTO(transaction);
    }

    @Override
    public TransactionDTO createTransaction(TransactionCreateDTO transactionCreateDTO) {
        log.info("Creating new transaction for player id: {}", transactionCreateDTO.getPlayerId());
        
        Player player = playerRepository.findById(transactionCreateDTO.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", transactionCreateDTO.getPlayerId()));
        
        Transaction transaction = TransactionMapper.toEntity(transactionCreateDTO);
        transaction.setPlayer(player);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with id: {}", savedTransaction.getId());
        
        return TransactionMapper.toDTO(savedTransaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with id: {}", id);
        
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }
        
        transactionRepository.deleteById(id);
        log.info("Transaction deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPlayerId(Long playerId) {
        log.info("Fetching transactions for player id: {}", playerId);
        
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("Player", "id", playerId);
        }
        
        return transactionRepository.findByPlayerId(playerId).stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
