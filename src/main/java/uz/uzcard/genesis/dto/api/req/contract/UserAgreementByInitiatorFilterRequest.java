package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * @author Javohir Elmurodov
 * @created 30/09/2020 - 9:38 AM
 * @project GTL
 */
@Getter
@Setter
public class UserAgreementByInitiatorFilterRequest extends FilterBase {
    private String codeSearch;
}
