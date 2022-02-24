package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RentRequest implements Serializable {

    @ApiModelProperty(required = true)
    private Long productItemId;

    @ApiModelProperty(required = true)
    private Double count;

    @ApiModelProperty(required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date expireDate;

    @ApiModelProperty(required = true)
    private Long warehouseId;
}
