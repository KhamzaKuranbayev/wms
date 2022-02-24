package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductFilterItemRequest extends FilterBase {
    private String name;
    private Long group_id;
    private Long type_id;
    private Long contractId;
    private Long takenAwayUserId;
    private Long productId;
    private Long warehouseId;
    private List<Long> warehouseIds;
    private String orderNumb;
    private String orderItemNumb;
    private String contractNumb;
    private String state;
    private String accountingCode;

    /*for only state PRODUCT_USED*/
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;

    private boolean isAttribute = false;
    private boolean isWithOutAttribute = false;

    private boolean forPrintQrCode;
    private String lotName;
    private Boolean used;
}