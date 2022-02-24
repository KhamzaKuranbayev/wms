package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StillageListFilterRequest implements Serializable {
    private Long warehouseId;
    private String name;
}