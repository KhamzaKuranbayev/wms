package uz.uzcard.genesis.dto.api.req.ESignature;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 12.10.2020  15:34
 */
@Getter
@Setter
public class PublicKeyDto {
    private String keyAlgName;
    private String publicKeyPublicKey;
}
