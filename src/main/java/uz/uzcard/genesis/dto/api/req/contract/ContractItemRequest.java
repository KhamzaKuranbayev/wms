package uz.uzcard.genesis.dto.api.req.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.hibernate.enums.SupplyType;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ContractItemRequest implements Serializable {
    private Long id;
    private Double count;
    private Long unitTypeId;
    private Long parentId;
    private Long orderItemId;
    private Long productId;
    private Long supplierId;
    private SupplyType supplyType;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date guessReceiveDate;
}