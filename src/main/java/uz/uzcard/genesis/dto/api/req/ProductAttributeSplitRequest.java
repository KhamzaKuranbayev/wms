package uz.uzcard.genesis.dto.api.req;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductAttributeSplitRequest implements Serializable {
    private Long productId;
    private String productName;
    private List<String> attributes = new ArrayList<>();
}
