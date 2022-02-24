package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by norboboyev_h  on 13.08.2020  17:21
 */
@Getter
@Setter
public class ContractInitiatorNotificationRequest {
    private List<Long> userIds;
    private Long contractItemId;
}
