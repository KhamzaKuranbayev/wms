package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 08.09.2020  11:21
 */
@Getter
@Setter
public class InventarizationLogRequest implements Serializable {

    private Long inventarizationId;

    private Long productItemId;

    private boolean isValid;

    private Double count;
}
