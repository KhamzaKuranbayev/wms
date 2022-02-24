package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OrderItemRejectRequest implements Serializable {
    @ApiModelProperty(required = true)
    private Long orderItemId;
    private Double value;
    @ApiModelProperty(required = true)
    private String reasen;
}