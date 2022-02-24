package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * Created by norboboyev_h  on 25.12.2020  15:42
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class ProduceHistoryRequest {
    private Long orderItemId;
    private double count;
    private Date guessedTakenAwayDate;
}
