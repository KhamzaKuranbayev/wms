package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by norboboyev_h  on 26.10.2020  18:07
 */
@Getter
@Setter
public class ProductQrCodeDetailList {
    private List<Long> ids;
}
