package uz.uzcard.genesis.dto.api.resp;

import uz.uzcard.genesis.dto.CoreMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class ParentChildResponse extends LinkedHashMap<String, Object> implements IResponse {
    public ParentChildResponse(Map<String, String> map) {
        putAll(map);
    }

    public ParentChildResponse(CoreMap map) {
        putAll(map.getInstance());
        putAll(map.getInstance2());
    }

    public static ParentChildResponse of(CoreMap map) {
        return new ParentChildResponse(map);
    }

    public void add(ParentChildResponse response) {
        if (get("items") == null) {
            put("items", new LinkedList<ParentChildResponse>());
        }
        ((LinkedList<ParentChildResponse>) get("items")).add(response);
    }

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
        if (get("errors") == null) {
            put("errors", new HashMap<String, String>());
        }
        ((HashMap<String, String>) get("errors")).put(key, value);
        return this;
    }
}