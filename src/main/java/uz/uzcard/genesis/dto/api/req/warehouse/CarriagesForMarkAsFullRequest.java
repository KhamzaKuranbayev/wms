package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by norboboyev_h  on 02.11.2020  12:25
 */
@Getter
@Setter
public class CarriagesForMarkAsFullRequest {
    private List<Long> ids;
}
