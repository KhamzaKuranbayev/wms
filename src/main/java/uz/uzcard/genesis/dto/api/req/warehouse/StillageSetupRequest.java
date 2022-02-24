package uz.uzcard.genesis.dto.api.req.warehouse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class StillageSetupRequest implements Serializable {
//    private Long id;
    private Long warehouseId;
    private int columnCount;
    private List<Long> rowCounts;
    private String name;
    private List<Long> cellIds;
    @ApiModelProperty(hidden = true)
    private boolean preview;
    private Integer width;
    private Integer height;
}