package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by norboboyev_h  on 08.10.2020  13:58
 */
@Getter
@Setter
public class ProductItemQrCodeList implements Serializable {
    //fixme qrcode
    private List<Long> productItemIds;
    private Long lotId;
}
