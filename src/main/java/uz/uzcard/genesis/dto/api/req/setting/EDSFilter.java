package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.EDSActionType;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 24.09.2020  15:37
 */
@Getter
@Setter
public class EDSFilter implements Serializable {

    private Long orderId;

    private Long contractItemId;

    private EDSActionType actionType;
}
