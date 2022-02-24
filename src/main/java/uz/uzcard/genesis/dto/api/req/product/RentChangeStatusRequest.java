package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RentChangeStatusRequest implements Serializable {
    private Long rentId;
    private Double count;
}
