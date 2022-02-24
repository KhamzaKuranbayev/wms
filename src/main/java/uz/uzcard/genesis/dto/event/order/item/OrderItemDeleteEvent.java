package uz.uzcard.genesis.dto.event.order.item;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 27.10.2020
 */
@Getter
@Setter
public class OrderItemDeleteEvent {

    private final Long orderId;

    private final Long orderItemId;

    private final List<String> userNames;

    public OrderItemDeleteEvent(Long orderId, Long orderItemId, List<String> userNames) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.userNames = userNames;
    }
}
