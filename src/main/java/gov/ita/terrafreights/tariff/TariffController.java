package gov.ita.terrafreights.tariff;

import gov.ita.terrafreights.tariff.country.Country;
import gov.ita.terrafreights.tariff.country.CountryRepository;
import gov.ita.terrafreights.tariff.stagingbasket.StagingBasket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

@RestController
public class TariffController {

  private final TariffRepository tariffRepository;
  private TariffCsvTranslator tariffCsvTranslator;
  private final TariffPersister tariffPersister;
  private CountryRepository countryRepository;
  private RestTemplate restTemplate;

  public TariffController(TariffRepository tariffRepository,
                          TariffCsvTranslator tariffCsvTranslator,
                          TariffPersister tariffPersister,
                          CountryRepository countryRepository,
                          RestTemplate restTemplate) {
    this.tariffRepository = tariffRepository;
    this.tariffCsvTranslator = tariffCsvTranslator;
    this.tariffPersister = tariffPersister;
    this.countryRepository = countryRepository;
    this.restTemplate = restTemplate;
  }

  @GetMapping("/api/tariffs")
  public Page<Tariff> tariffs(Pageable pageable,
                              @RequestParam("countryCode") String countryCode,
                              @RequestParam("stagingBasketId") Long stagingBasketId,
                              @RequestParam(value = "tariffLine", defaultValue = "") String tariffLine) {
    if (stagingBasketId != -1 && tariffLine.equals(""))
      return tariffRepository.findByCountryCodeAndStagingBasketId(countryCode, stagingBasketId, pageable);

    if (stagingBasketId != -1)
      return tariffRepository.findByCountryCodeAndStagingBasketIdAndTariffLineContaining(
        countryCode, stagingBasketId, tariffLine, pageable);

    if (!tariffLine.equals(""))
      return tariffRepository.findByCountryCodeAndTariffLineContaining(countryCode, tariffLine, pageable);

    return tariffRepository.findByCountryCode(countryCode, pageable);
  }

  @GetMapping("/api/tariffs/all")
  public List<Tariff> tariffs(Pageable pageable, @RequestParam("countryCode") String countryCode) {
    return tariffRepository.findByCountryCode(countryCode);
  }

  @GetMapping("/api/tariff")
  public Optional<Tariff> tariff(@RequestParam("tariffId") Long tariffId) {
    return tariffRepository.findById(tariffId);
  }

  @GetMapping("/api/tariff/staging_baskets")
  public List<StagingBasket> stagingBaskets(@RequestParam("countryCode") String countryCode) {
    return tariffRepository.findAllStagingBasketsByCountry(countryCode);
  }

  @GetMapping("/api/tariff/counts_by_country")
  public List<TariffCount> tariffCountsByCountry() {
    return tariffRepository.tariffCountsByCountry();
  }

  @PreAuthorize("hasRole('ROLE_EDSP')")
  @PutMapping("/api/tariffs/save")
  public String saveTariffs(@RequestParam("countryCode") String countryCode,
                            @RequestBody TariffUpload tariffUpload) {
    tariffRepository.deleteByCountry(countryCode);
    try {
      List<Tariff> tariffs = tariffCsvTranslator.translate(countryCode, new StringReader(tariffUpload.csv));
      tariffPersister.persist(tariffs);
      Country country = countryRepository.findByCode(countryCode);
      restTemplate.getForEntity(country.getEndpointmeFreshenUrl(), EndPointMeResponse.class);
      return "success";
    } catch (InvalidCsvFileException e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }
}
