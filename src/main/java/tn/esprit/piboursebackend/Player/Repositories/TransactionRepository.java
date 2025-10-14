package tn.esprit.piboursebackend.Player.Repositories;

import tn.esprit.piboursebackend.Player.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
