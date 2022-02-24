package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by norboboyev_h  on 16.12.2020  11:51
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class ProductItemIncomeReq {
    private Long qrCode;
    private double count;
    private Long productId;
    private Long unitTypeId;
    private Integer packageCount;
    @ApiModelProperty(hidden = true)
    private boolean used = false;
    private List<CarriagesReq> carriages;
    private Long warehouseId;

    @Getter
    @Setter
    public static class CarriagesReq implements Serializable {
        private Long carriageId;
        private boolean isFull;
    }
}
