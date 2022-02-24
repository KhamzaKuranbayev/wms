package uz.uzcard.genesis.dto.api.req.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

/**
 * Madaminov Javohir {02.12.2020}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemPickUpTimeFilterRequest extends FilterBase {
    private Long orderItemId;
}
