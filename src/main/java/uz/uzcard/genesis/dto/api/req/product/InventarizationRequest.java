package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 08.09.2020  11:36
 */
@Getter
@Setter
public class InventarizationRequest {
    private Long id;

    private boolean isStarted;

    private boolean isEnded;


}
