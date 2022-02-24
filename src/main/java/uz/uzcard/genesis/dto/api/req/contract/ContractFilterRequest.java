package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.hibernate.enums.SupplyType;

import java.util.Date;

@Getter
@Setter
public class ContractFilterRequest extends FilterBase {

    private String code;
    private String codeSearch;
    private Long groupId;
    private Long typeId;
    private String productName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date toDate;
    private String inisator;
    private Long supplierId;
    private SupplyType supplierType;
    private String status;
    @ApiModelProperty(name = "Request number - Order number")
    private String requestNumber;
    private boolean isMakeContract;
    private boolean forMobile;
    private boolean forQrCodePrint;

    @ApiModelProperty(hidden = true)
    public boolean isFiltered() {
        return true;
        /*return !StringUtils.isEmpty(code) || groupId != null
                || typeId != null || !StringUtils.isEmpty(productName)
                || fromDate != null || toDate != null
                || !StringUtils.isEmpty(inisator)
                || supplierId != null || !StringUtils.isEmpty(supplierType)
                || !StringUtils.isEmpty(status)
                || !StringUtils.isEmpty(requestNumber);*/
    }

    public ContractItemFilterRequst wrapIremRequst(Long contractId) {
        ContractItemFilterRequst request = new ContractItemFilterRequst();
        request.setContractId(contractId);
        request.setContractCode(code);
        request.setGroupId(groupId);
        request.setContractStatus(status);
        request.setProductName(productName);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setTypeId(typeId);
        request.setInisator(inisator);
        request.setForPrintQrCode(forQrCodePrint);
        return request;
    }
}