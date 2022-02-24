package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class DeleteRequest implements Serializable {

    @ApiModelProperty(required = true)
    private Long objectId;

    private String reason;
}
