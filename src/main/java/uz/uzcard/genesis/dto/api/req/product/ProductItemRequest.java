package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 15.08.2020  18:05
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductItemRequest implements Serializable {
    private Long partitionId;
    private Double count;
    private Integer packageCount;
    private Long lotId;
    private Long unitTypeId;
    private boolean used;
}