package uz.uzcard.genesis.dto.api.req.order;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 08.10.2020  10:36
 */
@Getter
@Setter
public class GivenProductOrderItemFilter extends FilterBase {
    private Integer numbSearch;
    private String allSearch;
}
