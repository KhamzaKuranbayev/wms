package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.EDSActionType;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 24.09.2020  17:03
 */
@Getter
@Setter
public class EDSRequest implements Serializable {

    private Long contractItemId;

    private Long userAgreementId;

    private Long orderItemId;

    private EDSActionType actionType;

    private String hashCode;

    private String documentParams;
}
