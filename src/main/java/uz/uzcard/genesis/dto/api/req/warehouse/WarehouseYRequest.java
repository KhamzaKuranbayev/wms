package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.PlaceType;

import java.io.Serializable;

@Getter
@Setter
public class WarehouseYRequest implements Serializable {
    private Long id;
    private PlaceType placeType;
}