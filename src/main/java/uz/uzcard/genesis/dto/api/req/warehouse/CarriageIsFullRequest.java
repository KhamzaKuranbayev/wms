package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Created by norboboyev_h  on 14.09.2020  10:34
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CarriageIsFullRequest {

    private boolean isFull;

    private Long carriageId;
}
