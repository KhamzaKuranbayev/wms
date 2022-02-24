package uz.uzcard.genesis.dto.api.req.partition;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PartitionProductRequest implements Serializable {
    private Long partitionId;
    private Double count;
    private Long orderItemId;
    private Long produceHistoryId;
    private Long notificationId;
}