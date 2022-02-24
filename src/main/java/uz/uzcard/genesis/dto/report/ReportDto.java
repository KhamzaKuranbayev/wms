package uz.uzcard.genesis.dto.report;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ReportDto implements Serializable {
    private String accounTingCode;
    private String productName;
    private String creationDate;
    private String updatedDate;
    private Double price;

    private Double newProductCount;
    private Double newTotalPrice;

    private Double producedProductCount;
    private Double producedTotalPrice;

    private Double lastProductCount;
    private Double lastTotalPrice;

    private Double total;
    private Double totalPrice;

    private String productTypeName;
    private String nameUz;
    private String nameEn;
    private String nameRu;
    private String nameCyrl;

}
