package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Response implements IResponse {
    protected boolean success;
    protected int code;
    protected Map<String, String> errors;

    public Response() {
    }

    public Response(int code) {
        this.code = code;
    }

    @Override
    public IResponse add(String key, String value) {
        if (errors == null) errors = new HashMap<>();
        errors.put(key, value);
        return this;
    }
}