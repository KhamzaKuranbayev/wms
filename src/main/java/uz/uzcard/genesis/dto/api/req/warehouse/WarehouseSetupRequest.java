package uz.uzcard.genesis.dto.api.req.warehouse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.PlaceType;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class WarehouseSetupRequest implements Serializable {
    private Long id;
    private Long columnCount;
    private List<List<Cell>> rows;
    @ApiModelProperty(hidden = true)
    private boolean preview;

    public List<Cell> get(Integer column) {
        if (rows == null || rows.size() <= column)
            return null;
        return rows.get(column);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Cell implements Serializable {
        private Integer position;
        private PlaceType placeType;
    }
}