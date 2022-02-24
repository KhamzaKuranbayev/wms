package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class ProductItemByCellFilterRequest extends FilterBase {
    private Long cellId;
    private String name;
}