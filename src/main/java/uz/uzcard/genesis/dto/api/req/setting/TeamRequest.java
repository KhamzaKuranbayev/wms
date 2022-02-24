package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TeamRequest implements Serializable {

    private Long id;

    @ApiModelProperty(required = true)
    private String name;

    @ApiModelProperty(required = true)
    private Long teamLeader;

    @ApiModelProperty(required = true)
    private List<Long> departments;
}
