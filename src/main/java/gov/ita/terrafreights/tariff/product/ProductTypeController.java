package gov.ita.terrafreights.tariff.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductTypeController {

  private ProductTypeRepository productTypeRepository;

  public ProductTypeController(ProductTypeRepository productTypeRepository) {
    this.productTypeRepository = productTypeRepository;
  }

  @GetMapping("/api/product_types/all")
  public List<ProductType> productTypes() {
    return productTypeRepository.findAll();
  }

}