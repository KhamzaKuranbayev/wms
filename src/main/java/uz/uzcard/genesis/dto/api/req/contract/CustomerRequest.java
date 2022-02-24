package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CustomerRequest implements Serializable {

    private Long id;

    @ApiModelProperty(required = true)
    private String name;
}
