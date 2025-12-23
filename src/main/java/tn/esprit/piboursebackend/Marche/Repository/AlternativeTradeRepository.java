package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.AlternativeTrade;

import java.util.List;

@Repository
public interface AlternativeTradeRepository extends JpaRepository<AlternativeTrade, Long> {

    List<AlternativeTrade> findBySession_SessionId(String sessionId);

    @Query("SELECT a FROM AlternativeTrade a WHERE a.session.sessionId = :sessionId AND a.symbol = :symbol")
    List<AlternativeTrade> findBySessionAndSymbol(@Param("sessionId") String sessionId,
                                                  @Param("symbol") String symbol);

    @Query("SELECT a.symbol, COUNT(a) FROM AlternativeTrade a WHERE a.session.sessionId = :sessionId GROUP BY a.symbol")
    List<Object[]> countTradesBySymbol(@Param("sessionId") String sessionId);
}