package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SplitProductItemRequest implements Serializable {
    private Long qrCode;
    private Double count;
    private boolean isNew;
    private Long qrCode2;
}