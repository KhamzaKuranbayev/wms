package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by norboboyev_h  on 25.12.2020  11:35
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class NotificationFromDepToOMTKReq implements Serializable {
    private Long orderItemId;
    private double count;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date guessedTakenAwayDate;
    private String body;

}
