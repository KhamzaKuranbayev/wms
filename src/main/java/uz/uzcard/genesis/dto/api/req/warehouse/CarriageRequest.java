package uz.uzcard.genesis.dto.api.req.warehouse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 19.08.2020  19:31
 */
@Getter
@Setter
@NoArgsConstructor
public class CarriageRequest {
    private Long stillageColumnId;
    private int carriageWidth;
    private int carriageHeight;
    private int carriageDepth;
    @ApiModelProperty(hidden = true)
    private boolean preview;

    public CarriageRequest(Long stillageColumnId, boolean preview) {
        this.stillageColumnId = stillageColumnId;
        this.preview = preview;
    }
}