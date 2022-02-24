package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ContractItemChangeStatusRequest implements Serializable {

    @ApiModelProperty(required = true)
    private Long contractItemId;
    @ApiModelProperty(required = true)
    private String description;
    @ApiModelProperty(required = true, value = "Accept count")
    private double accept;
    @ApiModelProperty(required = true, value = "Reject count(Partiya bilan kirim qilganda)")
    private double reject;
}
