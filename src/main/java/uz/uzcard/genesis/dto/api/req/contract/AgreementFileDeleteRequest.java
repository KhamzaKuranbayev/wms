package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AgreementFileDeleteRequest implements Serializable {

    private String name;
    private Long contractId;
}
