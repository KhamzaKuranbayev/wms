package uz.uzcard.genesis.dto.api.req.product;

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
@SuperBuilder
public class ProductAttributesRequest implements Serializable {
    private Long productId;
    private Long productGroupId;
    private List<AttributeRequest> attributes = new ArrayList<>();
}