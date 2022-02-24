package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class OutGoingContractRequest implements Serializable {

    @ApiModelProperty(required = true)
    private String contractNumber;

    @ApiModelProperty(required = true)
    private Long customerId;

    @ApiModelProperty(required = true)
    private Long productId;

    //    @ApiModelProperty(name = "количество")
    private Double requestCount;

    private Long unitTypeId;

    @ApiModelProperty(example = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeContractDate;

    @ApiModelProperty(example = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeDate;

}
