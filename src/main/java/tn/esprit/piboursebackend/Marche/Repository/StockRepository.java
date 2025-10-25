package tn.esprit.piboursebackend.Marche.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.piboursebackend.Marche.Entity.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findBySector(String sector);
    List<Stock> findByMarket_Code(String marketCode);

    @Query("SELECT s FROM Stock s WHERE s.currentPrice BETWEEN :minPrice AND :maxPrice ORDER BY s.currentPrice")
    List<Stock> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT s FROM Stock s WHERE s.marketCap > :minMarketCap ORDER BY s.marketCap DESC")
    List<Stock> findByMarketCapGreaterThan(@Param("minMarketCap") BigDecimal minMarketCap);

    @Query("SELECT DISTINCT s.sector FROM Stock s")
    List<String> findAllSectors();

    @Query("SELECT s FROM Stock s WHERE s.companyName LIKE %:companyName%")
    List<Stock> findByCompanyNameContaining(@Param("companyName") String companyName);
}