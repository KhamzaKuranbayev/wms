package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OrderItemsRejectRequest implements Serializable {

    @ApiModelProperty(required = true)
    private List<Long> itemIds;
    @ApiModelProperty(required = true)
    private String reasen;
}
