package ordersService.domain.request;

import com.fasterxml.jackson.databind.JsonNode;

public record OrderRequest(
    String orderId,
    String productType,
    JsonNode payload
) {

}
