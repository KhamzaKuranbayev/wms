package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class NotificationSendEveryOneRequest implements Serializable {

    private String title;
    private String comment;
}
