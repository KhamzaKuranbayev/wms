package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 22.08.2020  14:48
 */
@Getter
@Setter
public class UserAgreementReadAcceptRequest implements Serializable {

    private Long contractItemId;

    private boolean read;

    private boolean arrived;
}
