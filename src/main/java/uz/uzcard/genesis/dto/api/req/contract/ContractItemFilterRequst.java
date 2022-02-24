package uz.uzcard.genesis.dto.api.req.contract;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;

import java.util.Date;

@Getter
@Setter
public class ContractItemFilterRequst extends FilterBase {

    private Long contractId;
    private Long productId;
    private String contractCode;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
    private Long groupId;
    private Long typeId;
    private String productName;
    private String inisator;
    private String contractStatus;
    private String state;
    private Long unitTypeId;
    private boolean forPrintQrCode;
    private Boolean forQRStatus = null;
    private String orderNumb;
}