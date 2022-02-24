package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Created by norboboyev_h  on 24.12.2020  14:03
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class NotificationFilterRequest extends FilterBase {
    private String notificationCategoryType;
}
