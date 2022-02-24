package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Created by norboboyev_h  on 19.08.2020  17:56
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PutToCarriageRequest {
    private List<Long> productItemIds;
    private List<Long> carriageIds;
}
