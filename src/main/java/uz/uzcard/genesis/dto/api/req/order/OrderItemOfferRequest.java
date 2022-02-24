package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOfferRequest implements Serializable {
    @ApiModelProperty(required = true)
    private Long id;
    @ApiModelProperty(required = true)
    private String comment;
    @ApiModelProperty(required = true)
    private Long product_id;
    @ApiModelProperty(required = true)
    private Double count;
    @ApiModelProperty(required = true)
    private Long unitTypeId;
}
