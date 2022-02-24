package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 07.07.2020  16:48
 */
@Getter
@Setter
public class FirebaseRequest implements Serializable {

    @ApiModelProperty(required = true)
    private String token;

    @ApiModelProperty(required = true)
    private String deviceId;

}
