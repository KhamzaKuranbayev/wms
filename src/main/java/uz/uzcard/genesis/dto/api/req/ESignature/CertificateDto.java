package uz.uzcard.genesis.dto.api.req.ESignature;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by norboboyev_h  on 12.10.2020  15:35
 */
@Getter
@Setter
public class CertificateDto {
    private String subjectName;
    private String validFrom;
    private String validTo;
    private PublicKeyDto publicKey;
}
