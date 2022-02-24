package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class StillageColumnFilterRequest extends FilterBase {
    private Long stillage_id;
}
