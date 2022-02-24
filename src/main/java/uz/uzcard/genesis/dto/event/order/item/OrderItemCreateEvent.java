package uz.uzcard.genesis.dto.event.order.item;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._OrderItem;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 20.10.2020
 */

@Getter
@Setter
public class OrderItemCreateEvent {

    private final Long orderId;

    private final _OrderItem orderItem;

    private final List<String> userNames;

    private final String eventType;

    public OrderItemCreateEvent(Long orderId, _OrderItem orderItem, List<String> userNames, String eventType) {
        this.orderId = orderId;
        this.orderItem = orderItem;
        this.userNames = userNames;
        this.eventType = eventType;
    }
}
