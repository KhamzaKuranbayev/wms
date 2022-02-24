package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 25.12.2020  15:38
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class ProduceHistoryFilter extends FilterBase {
    private Long orderItemId;

}
