package uz.uzcard.genesis.dto.api.req.ESignature;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by norboboyev_h  on 12.10.2020  15:37
 */
@Getter
@Setter
public class SignersDto {
    private List<CertificateDto> certificate;
    private boolean verified;
    private boolean certificateVerified;
    private List<String> policyIdentifiers;
    private String signingTime;
}
