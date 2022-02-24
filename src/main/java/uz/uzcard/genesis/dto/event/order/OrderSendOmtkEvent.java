package uz.uzcard.genesis.dto.event.order;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._Order;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 21.10.2020
 */
@Getter
@Setter
public class OrderSendOmtkEvent {

    private final _Order order;

    private final List<String> userNameOmtk;

    public OrderSendOmtkEvent(_Order order, List<String> userNameOmtk) {
        this.order = order;
        this.userNameOmtk = userNameOmtk;
    }
}
