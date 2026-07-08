package ordersService.domain.response;

import java.util.List;

public record OrderListResponse(List<OrderResponse> orders, int total) {

}
