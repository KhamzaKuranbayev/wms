package uz.uzcard.genesis.dto.api.req.order;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OrderItemAcceptRequest implements Serializable {
    private Long orderItemId;
//    private Double value;
}