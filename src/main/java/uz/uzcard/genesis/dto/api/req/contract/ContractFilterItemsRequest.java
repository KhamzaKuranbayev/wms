package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class ContractFilterItemsRequest extends FilterBase {
    private String code;
}