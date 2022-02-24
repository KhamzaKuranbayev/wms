package uz.uzcard.genesis.dto.api.req.partition;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 14.08.2020  15:28
 */
@Getter
@Setter
public class PartitionFilterRequest extends FilterBase {
    private Long orderId;
    private Long productId;
    private String warehouseNameSearch;
    private String warehouseId;
}
