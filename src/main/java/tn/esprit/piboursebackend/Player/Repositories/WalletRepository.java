package tn.esprit.piboursebackend.Player.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.piboursebackend.Player.Entities.Wallet;

public interface WalletRepository extends JpaRepository<Wallet,Long> {
}
