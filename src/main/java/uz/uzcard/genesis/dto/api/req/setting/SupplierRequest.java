package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SupplierRequest implements Serializable {
    private Long id;
    private String name;
}