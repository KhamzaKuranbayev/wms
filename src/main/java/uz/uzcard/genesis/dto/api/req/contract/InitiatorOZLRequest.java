package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by norboboyev_h  on 26.12.2020  17:17
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class InitiatorOZLRequest implements Serializable {
    @ApiModelProperty(hidden = true)
    private Long contractItemId;
    private List<Long> userIds;
}
