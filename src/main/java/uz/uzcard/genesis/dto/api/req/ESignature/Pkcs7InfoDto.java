package uz.uzcard.genesis.dto.api.req.ESignature;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by norboboyev_h  on 12.10.2020  15:40
 */
@Getter
@Setter
public class Pkcs7InfoDto {
    private List<SignersDto> signers;
    private String documentBase64;
}
