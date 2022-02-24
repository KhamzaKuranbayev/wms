package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UseProductItemRequest implements Serializable {
    @ApiModelProperty(required = true)
    private Long productItemId;
    @ApiModelProperty(required = true)
    private Double count;
    private String comment;
}
