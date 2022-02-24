package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
public class OutGoingContractFilterRequest extends FilterBase {
    private String contractNumber;

    private Long supplierId;

    private Long customerId;

    private Long productId;

    private Double requestCount;

    private Double contractBalance;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromCloseContractDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toCloseContractDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromCloseDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toCloseDate;

    private boolean isCompleted;

    private boolean isNotCompleted;
}
