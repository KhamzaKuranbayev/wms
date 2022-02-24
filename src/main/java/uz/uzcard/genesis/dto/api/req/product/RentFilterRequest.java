package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RentFilterRequest extends FilterBase {
    private Long productItemId;
    private Long productId;
    private Long departmentId;
    private Boolean withReturned = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
}
