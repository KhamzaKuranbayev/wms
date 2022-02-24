package uz.uzcard.genesis.dto.api.req.partition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class LotAddRequest implements Serializable {
    private Long contractItemId;
    private String lotName;
    private String actNo;
    private String gtd;
    private String invoiceNo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date invoiceDate;
    private Integer packageCount;
    private double count;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date date;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date productCreationDate;
    private Long warehouseId;
}