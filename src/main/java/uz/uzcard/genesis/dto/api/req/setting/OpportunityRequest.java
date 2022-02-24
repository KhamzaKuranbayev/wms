package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OpportunityRequest implements Serializable {

    @ApiModelProperty(required = true, example = "1")
    private Long orderItemId;
    @ApiModelProperty(required = true, example = "0")
    private Long rejectProductCount;
    @ApiModelProperty(required = true, example = "0")
    private Long provideProductCount;
    @ApiModelProperty(required = true, example = "0")
    private Long contractProductCount;

    private String reason;
}
