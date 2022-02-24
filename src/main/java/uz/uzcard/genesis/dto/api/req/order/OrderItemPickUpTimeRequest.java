package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Madaminov Javohir {02.12.2020}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemPickUpTimeRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date pickUpTime;

    private Long orderItemId;
}
