package uz.uzcard.genesis.dto.api.req.history;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

/**
 * Created by 'Madaminov Javohir' on 13.11.2020
 */
@Getter
@Setter
@ApiModel
public class OrderHistoryRequest extends FilterBase {
    private Integer numb;
    private Integer itemNumb;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toDate;
    private String state;

    private Long orderId;
}
