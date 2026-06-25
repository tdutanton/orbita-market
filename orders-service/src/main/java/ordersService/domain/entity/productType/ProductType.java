package ordersService.domain.entity.productType;

import lombok.Getter;
import ordersService.exceptions.order.UnknownProductTypeException;

@Getter
public enum ProductType {
  ARCHIVE("ARCHIVE"),
  TASKING("TASKING"),
  MONITORING("MONITORING");


  private final String name;

  ProductType(String name) {
    this.name = name;
  }

  public static void validateFromName(String name) {
    if (name == null) {
      throw new UnknownProductTypeException("Тип продукта отсутствует");
    }
    for (ProductType type : values()) {
      if (type.name.equals(name)) {
        return;
      }
    }
    throw new UnknownProductTypeException("Неподдерживаемый тип продукта: " + name);
  }
}
