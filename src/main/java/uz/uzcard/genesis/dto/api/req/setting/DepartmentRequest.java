package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class DepartmentRequest implements Serializable {
    private Long id;

    @ApiModelProperty(required = true)
    private String nameEn;
    @ApiModelProperty(required = true)
    private String nameRu;
    @ApiModelProperty(required = true)
    private String nameUz;

    private Long parentId;

    private List<Long> warehouseIds;
    private OrderClassification depType;
}
