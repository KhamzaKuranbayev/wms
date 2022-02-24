package uz.uzcard.genesis.dto.api.req.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AttributeRequest implements Serializable {

    @ApiModelProperty(value = "AttributeID")
    Long id;
    @ApiModelProperty(value = "items value")
    private List<String> items = new ArrayList<>();
}
