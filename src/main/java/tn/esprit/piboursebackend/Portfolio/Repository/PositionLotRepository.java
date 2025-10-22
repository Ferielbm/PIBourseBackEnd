package tn.esprit.piboursebackend.Portfolio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import tn.esprit.piboursebackend.Portfolio.Entity.PositionLot;

import java.util.List;
import java.util.Optional;


@Repository
public interface PositionLotRepository extends JpaRepository<PositionLot, Long> {
    List<PositionLot> findByPosition_PositionIdOrderByAsOfAsc(Long positionId);

    List<PositionLot> findByPosition_PositionIdOrderByAsOfDesc(Long positionId);
}