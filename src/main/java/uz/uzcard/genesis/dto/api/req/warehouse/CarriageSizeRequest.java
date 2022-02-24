package uz.uzcard.genesis.dto.api.req.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CarriageSizeRequest {

    private Integer width;
    private Integer height;

    private List<Long> ids;
}
