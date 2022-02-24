package uz.uzcard.genesis.dto.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "service.prod")
public class ServerProperties {

    private String ip;

    private String port;
}
