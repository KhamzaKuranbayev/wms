package uz.uzcard.genesis.dto.api.req.partition;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PartitionRequest implements Serializable {
    private Long contractItemId;
    private Long warehouseId;
    private Long orderItemId;
    private Double count;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date date;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date productCreationDate;
    private Date expirationDate;
    private Integer packageCount;
    private String actNo;
    private String gtd;
    private String invoiceNo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date invoiceDate;

    public PartitionRequest(Long contractItemId,
                            Long warehouseId,
                            Long orderItemId,
                            Double count,
                            Integer packageCount,
                            Date date,
                            Date expirationDate) {
        this.contractItemId = contractItemId;
        this.warehouseId = warehouseId;
        this.orderItemId = orderItemId;
        this.count = count;
        this.packageCount = packageCount;
        this.expirationDate = expirationDate;
        this.date = date;
    }
}