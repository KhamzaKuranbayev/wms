package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipleOrderCreateRequest implements Serializable {
    private Long id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date timeToBeEntered;
    private List<MultipleOrderItemRequest> items = new ArrayList<>();
    private List<String> deletedFiles = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MultipleOrderItemRequest implements Serializable {
        private Long id;
        private Long product_id;
        @ApiModelProperty(required = true)
        private Long unitTypeId;
        private Double count;
        private boolean changed;

        public OrderItemRequest wrap(Long orderId) {
            OrderItemRequest request = new OrderItemRequest(id, orderId, product_id, count, unitTypeId);
            return request;
        }
    }
}