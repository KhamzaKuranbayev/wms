package uz.uzcard.genesis.dto.api.req;

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
public class FilterBase implements Serializable {
    @ApiModelProperty(position = 1)
    protected int page = 0;

    @ApiModelProperty(position = 2)
    protected int limit = 10;
    @ApiModelProperty(position = 3)
    protected String sortBy;
    @ApiModelProperty(position = 4)
    protected Boolean sortDirection;

    public int getLimit() {
        return limit > 200 ? 200 : limit;
    }
}