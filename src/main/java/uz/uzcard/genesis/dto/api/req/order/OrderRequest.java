package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest implements Serializable {
    private Long id;
    private Long product_id;
    private Double count;
    @ApiModelProperty(required = true)
    private Long unitTypeId;
    @ApiModelProperty(hidden = true)
    private boolean defaultYearly = false;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date timeToBeEntered;

    public OrderRequest(Long id, Long product_id, Double count, Long unitTypeId) {
        this.id = id;
        this.product_id = product_id;
        this.count = count;
        this.unitTypeId = unitTypeId;
    }

    public OrderItemRequest wrapOrderItemRequest(Long order_id) {
        return new OrderItemRequest(null, order_id, product_id, count, unitTypeId, timeToBeEntered);
    }
}