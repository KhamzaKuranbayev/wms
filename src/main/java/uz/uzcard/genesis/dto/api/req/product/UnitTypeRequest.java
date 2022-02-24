package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UnitTypeRequest implements Serializable {
    private Long id;
    @ApiModelProperty(required = true)
    private String nameEn;
    @ApiModelProperty(required = true)
    private String nameUz;
    @ApiModelProperty(required = true)
    private String nameRu;

    private boolean countable;
}
