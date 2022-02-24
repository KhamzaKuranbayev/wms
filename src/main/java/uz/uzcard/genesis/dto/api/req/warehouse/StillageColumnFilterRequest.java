package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class StillageColumnFilterRequest extends FilterBase {
    private Long stillageId;
    private int limit;
}
