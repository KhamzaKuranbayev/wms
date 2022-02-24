package uz.uzcard.genesis.dto.api.req.contract;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ContractUpdateRequest implements Serializable {

    @ApiModelProperty(required = true)
    private Long id;

    private Long supplierId;

    private String code;

    @ApiModelProperty(example = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date conclusionDate;
}
