package uz.uzcard.genesis.dto.event.order.item;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._OrderItem;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 27.10.2020
 */
@Getter
@Setter
public class OrderItemUpdateStatusEvent {
    private final Long orderId;

    private final _OrderItem orderItem;

    private final String status;

    private final String eventType;

    private final List<String> userNames;

    public OrderItemUpdateStatusEvent(Long orderId, _OrderItem orderItem, String status, String eventType, List<String> userNames) {
        this.orderId = orderId;
        this.orderItem = orderItem;
        this.status = status;
        this.userNames = userNames;
        this.eventType = eventType;
    }
}
