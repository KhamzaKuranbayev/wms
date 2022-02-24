package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 08.09.2020  13:15
 */
@Getter
@Setter
public class InventarizationLogFilter extends FilterBase {
    public Long inventarizationId;
    public Long productGroupId;
    public Long productTypeId;
    public Long userId;
    public Long productId;
    public Boolean valid;
}
