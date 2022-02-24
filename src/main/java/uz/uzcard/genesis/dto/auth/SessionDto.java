package uz.uzcard.genesis.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private String sessionToken;
    private String tokenType;
    private String refreshToken;
    private Long expiresIn;
    private String scope;
}
