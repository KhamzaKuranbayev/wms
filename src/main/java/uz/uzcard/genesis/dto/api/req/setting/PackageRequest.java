package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PackageRequest implements Serializable {
    private Long product_id;
    private Long packageType_id;
    private Integer width;
    private Integer height;
    private Integer depth;
}
