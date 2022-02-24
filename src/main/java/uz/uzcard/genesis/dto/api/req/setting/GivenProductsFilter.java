package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 26.09.2020  16:28
 */
@Getter
@Setter
public class GivenProductsFilter {

    private String contractCode;

    private Long orderNumb;

    private String productName;

    private Long warehouseId;
}
