package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 07.08.2020  9:48
 */
@Getter
@Setter
public class UserAgreementFilterRequest extends FilterBase {
    private Long contractItemId;
    private String status;
    private Long contractId;
    private String productNameSearch;
    private boolean forInitiator;
    private boolean forNotification;
}
