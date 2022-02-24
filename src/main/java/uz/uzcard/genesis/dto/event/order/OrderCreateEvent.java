package uz.uzcard.genesis.dto.event.order;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._Order;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 20.10.2020
 */
@Getter
@Setter
public class OrderCreateEvent {
    private final _Order order;

    private final List<String> userNames;

    public OrderCreateEvent(_Order order, List<String> userNames) {
        this.order = order;
        this.userNames = userNames;
    }
}
