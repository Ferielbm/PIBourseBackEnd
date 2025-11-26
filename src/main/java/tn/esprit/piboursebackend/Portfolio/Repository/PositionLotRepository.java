package tn.esprit.piboursebackend.Portfolio.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import tn.esprit.piboursebackend.Portfolio.Entity.PositionLot;

import java.util.List;
import java.util.Optional;


@Repository
public interface PositionLotRepository extends JpaRepository<PositionLot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from PositionLot l where l.position.positionId = :pid order by l.asOf asc")
    List<PositionLot> findByPosition_PositionIdOrderByAsOfAscForUpdate(Long positionId);

    List<PositionLot> findByPosition_PositionIdOrderByAsOfDesc(Long positionId);
}