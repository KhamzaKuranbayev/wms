package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 07.08.2020  10:01
 */
@Getter
@Setter
public class UserAgreementChangeStatusRequest implements Serializable {
    @ApiModelProperty(required = true)
    private Long userAgreementId;
    @ApiModelProperty(required = true)
    private String reason;
}
