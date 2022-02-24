package uz.uzcard.genesis.dto.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ReportByTypeDto implements Serializable {
    private String typeName;
    private List<ReportDto> childs;

    private Double newTotal;
    private Double newTotalPrice;

    private Double producedTotal;
    private Double producedTotalPrice;

    private Double totalCount;
    private Double totalPrice;

}
