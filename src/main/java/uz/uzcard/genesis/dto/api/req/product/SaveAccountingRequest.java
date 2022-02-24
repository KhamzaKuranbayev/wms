package uz.uzcard.genesis.dto.api.req.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SaveAccountingRequest implements Serializable {
    private Long id;
    private String accountingCode;
    private Double price;
}