package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductTypeFilterRequest extends FilterBase {

    private Long typeId;
    private String name;
}
