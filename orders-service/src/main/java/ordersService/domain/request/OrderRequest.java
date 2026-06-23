package ordersService.domain.request;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record OrderRequest(
    String productType,
    BigDecimal price,
    JsonNode payload
) {

}
