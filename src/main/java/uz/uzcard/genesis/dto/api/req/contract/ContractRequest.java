package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.hibernate.enums.SupplyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ContractRequest implements Serializable {

    private Long id;

    //    @ApiModelProperty(required = true)
    private String code;

    @ApiModelProperty(example = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date guessReceiveDate;

    @ApiModelProperty(example = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date conclusionDate;

    /**
     * Order Item
     */
    private Long orderItemId;
    private Long productId;
    private Double count;
    private Long unitTypeId;
    private Long supplierId;
    private SupplyType supplyType;

    private Long contractItemId;
    private List<Long> userIds;
    /**
     * Order Item List
     */
    private List<Long> orderItems = new ArrayList<>();

    public ContractItemRequest wrapContractItemRequest(Long contractId) {
        return new ContractItemRequest(contractItemId, count, unitTypeId, contractId, orderItemId, productId, supplierId, supplyType, guessReceiveDate);
    }
}
