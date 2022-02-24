package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 19.08.2020  18:41
 */
@Getter
@Setter
public class CarriageFilterRequest extends FilterBase {
    private Long stillageColumnId;
    private String allSearch;
}
