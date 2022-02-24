package uz.uzcard.genesis.dto.api.resp;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by 'Madaminov Javohir' on 30.10.2020
 */
public class BoardResponse extends LinkedHashMap<String, Object> implements IResponse {
    public BoardResponse(Map<String, String> map) {
        putAll(map);
    }

    public void add(BoardResponse response) {
        if (get("rows") == null) {
            put("rows", new LinkedList<BoardResponse>());
        }
        ((LinkedList<BoardResponse>) get("rows")).add(response);
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
        return null;
    }
}