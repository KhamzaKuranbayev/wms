package uz.uzcard.genesis.dto.event.order.item;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 27.10.2020
 */
@Getter
@Setter
public class OrderItemUpdateCountEvent {
    private final Long orderId;

    private final Long orderItemId;

    private final Double count;

    private final List<String> userNames;

    public OrderItemUpdateCountEvent(Long orderId, Long orderItemId, Double count, List<String> userNames) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.count = count;
        this.userNames = userNames;
    }
}
