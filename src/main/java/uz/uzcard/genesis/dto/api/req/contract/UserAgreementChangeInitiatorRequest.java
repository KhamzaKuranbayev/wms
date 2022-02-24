package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 07.08.2020  12:13
 */
@Getter
@Setter
public class UserAgreementChangeInitiatorRequest implements Serializable {
    private Long userAgreementId;
    private Long userId;
}
