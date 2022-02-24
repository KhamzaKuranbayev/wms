package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 12.08.2020  14:18
 */
@Getter
@Setter
public class UserAgreementSaveByContractRequest {
    private Long userId;
    private Long contractItemId;
}
