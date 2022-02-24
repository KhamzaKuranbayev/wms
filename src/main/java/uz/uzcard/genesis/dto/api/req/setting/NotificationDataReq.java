package uz.uzcard.genesis.dto.api.req.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 24.12.2020  11:25
 */
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDataReq implements Serializable {
    private Long contractItemId;
    private Long produceHistoryId;
    private Boolean isProduceHistoryStateDone;
    private String phoneNumberOMTK;
    private Double count;
    private Long orderItemId;
    private Long contractId;
    private String contractCode;
    private String omtkInfo;
    private String filePath;
}
