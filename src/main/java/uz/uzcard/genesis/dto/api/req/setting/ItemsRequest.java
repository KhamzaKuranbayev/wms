package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.List;

@Getter
@Setter
public class ItemsRequest extends FilterBase {

    //    @ApiModelProperty(required = true)
    private Long objectId;

    @ApiModelProperty(hidden = true)
    private OrderClassification type;

    private List<String> removeStatus;

    private String parentNumbSearch;

    private boolean forProducing;

    private String productName;

    private String positionState;

    private Long contractId;

    private String allSearch;

}
