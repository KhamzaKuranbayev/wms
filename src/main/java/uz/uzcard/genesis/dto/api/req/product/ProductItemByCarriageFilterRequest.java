package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by 'Madaminov Javohir' on 30.10.2020
 */
@Getter
@Setter
public class ProductItemByCarriageFilterRequest extends FilterBase {
    private Long carriageId;
    private String name;
}