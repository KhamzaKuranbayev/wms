package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Created by norboboyev_h  on 24.12.2020  10:52
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class NotificationFromOZLToOMTKReq {
    private String body;
    private Long contractItemId;
}
