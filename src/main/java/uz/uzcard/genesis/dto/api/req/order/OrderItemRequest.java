package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest implements Serializable {
    private Long id;
    private Long order_id;
    private Long product_id;
    private Double count;
    @ApiModelProperty(required = true)
    private Long unitTypeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date timeToBeEntered;

    public OrderItemRequest(Long id, Long order_id, Long product_id, Double count, Long unitTypeId) {
        this.id = id;
        this.order_id = order_id;
        this.product_id = product_id;
        this.count = count;
        this.unitTypeId = unitTypeId;
        this.timeToBeEntered = timeToBeEntered;
    }
}