package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

/**
 * Created by norboboyev_h  on 08.09.2020  13:13
 */
@Getter
@Setter
public class InventarizationFilter extends FilterBase {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date startToDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date startFromDate;

    private Long auditorId;
}
