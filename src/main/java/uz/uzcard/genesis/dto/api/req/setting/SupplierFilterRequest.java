package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SupplierFilterRequest extends FilterBase {
    private String name;
}