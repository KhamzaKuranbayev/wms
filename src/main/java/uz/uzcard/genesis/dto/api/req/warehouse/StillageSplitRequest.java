package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StillageSplitRequest {
    private Long stillage_id;
    private Integer columnCount;
}