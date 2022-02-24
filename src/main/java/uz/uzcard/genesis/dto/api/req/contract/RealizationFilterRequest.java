package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
public class RealizationFilterRequest extends FilterBase {
    private Long id;
    private String contractNumber;
    private Long realizatorId;
    private Long departmentId;
    private Long productId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
}
