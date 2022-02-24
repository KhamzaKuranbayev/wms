package uz.uzcard.genesis.dto.api.req.history;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

/**
 * @author Javohir Elmurodov
 * @created 20/10/2020 - 5:15 PM
 * @project GTL
 */

@Getter
@Setter
@ApiModel
public class ContractHistoryRequest extends FilterBase {
    private String code;
    @ApiModelProperty(value = "Contract Item API uchun")
    private Integer itemNumb;
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
//    private Date fromDate;
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
//    private Date toDate;
    private String state;
}
