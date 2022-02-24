package uz.uzcard.genesis.dto.api.req.setting;

import io.swagger.annotations.ApiModel;
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
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Attribute save request")
@SuperBuilder
public class AttributeRequest implements Serializable {
    private Long id;
    private String name;
    private List<String> items = new ArrayList<>();
}