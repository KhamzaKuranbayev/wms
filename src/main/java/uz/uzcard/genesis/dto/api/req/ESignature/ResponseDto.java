package uz.uzcard.genesis.dto.api.req.ESignature;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 12.10.2020  15:41
 */
@Getter
@Setter
public class ResponseDto {
    private boolean success;
    private Pkcs7InfoDto pkcs7Info;
}
