package uz.uzcard.genesis.dto.api.resp;

public class JwtResponse extends Response {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final Long expired;

    public JwtResponse(String jwttoken, Long expired) {
        this.jwttoken = jwttoken;
        this.expired = expired;
    }

    public String getToken() {
        return this.jwttoken;
    }

    public Long getExpired() {
        return expired;
    }
}