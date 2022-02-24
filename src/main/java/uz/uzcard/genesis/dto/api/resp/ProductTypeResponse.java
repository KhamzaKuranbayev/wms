package uz.uzcard.genesis.dto.api.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzcard.genesis.dto.CoreMap;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductTypeResponse extends Response {
    private Long id;
    private String name;
    private Type type;
    private Long parentId;
    @JsonIgnore
    private boolean hasParent = false;
    //    private List<ProductTypeResponse> types = new ArrayList<>();
    private List<ProductTypeResponse> child = new ArrayList<>();
    private List<CoreMap> unitTypes;
    private List<String> productAttrs = new ArrayList<>();

    public ProductTypeResponse(Long id, String name, Type type, Long parentId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
    }

    public ProductTypeResponse(Long id, String name, Type type, Long parentId, List<CoreMap> unitTypes, List<String> productAttrs) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.unitTypes = unitTypes;
        this.productAttrs = productAttrs;
    }

//    public void addType(ProductTypeResponse type) {
//        types.add(type);
//    }

    public void addChild(ProductTypeResponse product) {
        child.add(product);
    }

    public enum Type {
        Type,
        Product
    }
}