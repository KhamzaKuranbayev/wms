package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PackageByProductAndPackageTypeRequest implements Serializable {
    private Long product_id;
    private Long packageType_id;
}