package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by norboboyev_h  on 26.09.2020  19:41
 */
@Getter
@Setter
public class TakenProductRequest implements Serializable {

    private Long orderItemId;

    //fixme qrcode
    private List<Long> productItemIds;

    private boolean replace;

    private Long warehouseId;
}