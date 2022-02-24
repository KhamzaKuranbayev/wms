package uz.uzcard.genesis.dto.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductResponse extends HashMap<String, Object> implements IResponse {

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public Map<String, String> getErrors() {
        return null;
    }

    @Override
    public IResponse add(String key, String value) {
        return null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attributes {
        private Long id;
        private String name;
        private List<String> items = new ArrayList<>();
    }
}
