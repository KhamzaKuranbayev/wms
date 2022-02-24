package uz.uzcard.genesis.dto.api.req.warehouse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StillageRequest implements Serializable {
    private Long id;
    private Long warehouse_id;
    private String name;
    private String address;
    private int width;
    private int height;
    private int depth;
    @ApiModelProperty(value = "Ustunlar soni", example = "1")
    private int columnCount;
    @ApiModelProperty(value = "Qatorlar soni", example = "1")
    private int rowCount;
    //    private CarriageRequest carriageRequest;
    @ApiModelProperty(hidden = true)
    private boolean preview;
}
