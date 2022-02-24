package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ContractRejectRequest implements Serializable {

    @ApiModelProperty(required = true)
    private Long contractId;
    @ApiModelProperty(required = true)
    private String reason;
}
