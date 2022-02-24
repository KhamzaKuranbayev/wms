package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzcard.genesis.hibernate.enums.NotificationCategoryType;

/**
 * Created by norboboyev_h  on 07.07.2020  9:15
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NotificationRequest {

    private String body;

    private String token;

    private NotificationDataReq data;

    private String title;

    private NotificationCategoryType type;

    private String filePath;

}
