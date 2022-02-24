package uz.uzcard.genesis.dto.api.req.history;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ProductItemRequest implements Serializable {

    private String accountingCode;
    private Long id;
    private Long qrCode;
    /*@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toDate;*/
    private String state;

    private Long orderId;
    private Integer orderNumb;
}