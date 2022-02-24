package uz.uzcard.genesis.dto.event.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._Contract;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 10.11.2020
 */
@Getter
@Setter
public class ContractChangeStatusEvent {

    private final _Contract contract;

    private final String eventType;

    private final List<String> userNames;

    public ContractChangeStatusEvent(_Contract contract, String eventType, List<String> userNames) {
        this.contract = contract;
        this.eventType = eventType;
        this.userNames = userNames;
    }
}
