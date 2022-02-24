package uz.uzcard.genesis.dto.api.req.order;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OrderItemStateChangeRequest implements Serializable {
    private Long id;
    private String state;
}