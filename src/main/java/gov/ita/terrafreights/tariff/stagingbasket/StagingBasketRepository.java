package gov.ita.terrafreights.tariff.stagingbasket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagingBasketRepository extends JpaRepository<StagingBasket, Long> {
}