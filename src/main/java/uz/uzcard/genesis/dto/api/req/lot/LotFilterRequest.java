package uz.uzcard.genesis.dto.api.req.lot;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class LotFilterRequest extends FilterBase {
    private Long contractItemId;
    private String name;
}
