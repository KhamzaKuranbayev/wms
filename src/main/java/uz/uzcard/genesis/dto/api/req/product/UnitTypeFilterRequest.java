package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UnitTypeFilterRequest extends FilterBase {
    private String name;
}
