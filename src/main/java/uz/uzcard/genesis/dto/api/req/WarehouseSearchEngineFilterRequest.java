package uz.uzcard.genesis.dto.api.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseSearchEngineFilterRequest extends FilterBase {
    private String name;
}