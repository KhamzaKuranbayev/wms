package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class WarehouseRequest implements Serializable {
    private Long id;
    private String nameEn;
    private String nameRu;
    private String nameUz;
    private String address;
    private Long parentId;
//    private Long departmentId;
}