package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductRequest implements Serializable {
    private Long id;
    @ApiModelProperty(required = true)
    private String name;

    private String uniqueKey;
    @ApiModelProperty(required = false)
    private Long product_type_id;
    @ApiModelProperty(required = true)
    private Long product_group_id;
    @ApiModelProperty(required = true)
    private List<Long> unitTypeIds;
    @ApiModelProperty(required = true)
    private String packageType;
    @ApiModelProperty(value = "Srok. (second) 1 soat = 3600")
    private Long expiration;
    private Integer limitCount;
    private boolean fileDeleted;
}