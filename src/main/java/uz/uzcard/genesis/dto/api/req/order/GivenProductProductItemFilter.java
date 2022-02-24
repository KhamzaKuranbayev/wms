package uz.uzcard.genesis.dto.api.req.order;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.api.req.FilterBase;

@Getter
@Setter
public class GivenProductProductItemFilter extends FilterBase {
    private Long orderItemId;
    private Long orderId;
    private Long contractItemId;
    private Long contractId;
    private Long partionId;
    private String productName;
}
