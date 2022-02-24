package uz.uzcard.genesis.dto.event.contract;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.entity._Contract;

import java.util.List;

/**
 * Created by 'Madaminov Javohir' on 02.11.2020
 */
@Getter
@Setter
public class ContractCreateEvent {

    private final _Contract contract;

    private final List<String> userNames;

    private final String eventType;

    public ContractCreateEvent(_Contract contract, List<String> userNames, String eventType) {
        this.contract = contract;
        this.userNames = userNames;
        this.eventType = eventType;
    }
}
