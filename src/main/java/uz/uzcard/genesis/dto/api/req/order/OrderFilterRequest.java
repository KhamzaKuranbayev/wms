package uz.uzcard.genesis.dto.api.req.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.Date;

@ApiModel
@Getter
@Setter
public class OrderFilterRequest extends FilterBase {

    @ApiModelProperty(hidden = true)
    private OrderClassification ordertype;

    private String orderNumber;
    private Long groupId;
    private Long typeId;
    private String name;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
    private String contractNumber;
    private boolean forProduce;
    private String positionState;
    private String requestState;
    private Long statusChangedUser;
    private Long departmentId;
    private Long initiator;
    private Long supplierId;
    private boolean isHasContract;

    @ApiModelProperty(hidden = true)
    public boolean isFiltered() {
        return true;
        /*return OrderClassification.OZL.equals(ordertype) || !StringUtils.isEmpty(orderNumber) ||
                groupId != null ||
                typeId != null ||
                departmentId != null ||
                statusChangedUser != null ||
                !StringUtils.isEmpty(name) ||
                fromDate != null ||
                toDate != null ||
                !StringUtils.isEmpty(positionState) ||
                !StringUtils.isEmpty(contractNumber) ||
                !StringUtils.isEmpty(requestState);*/
    }
}