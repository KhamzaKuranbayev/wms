package uz.uzcard.genesis.dto.api.resp;

import uz.uzcard.genesis.dto.CoreMap;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by norboboyev_h  on 13.01.2021  15:37
 */
public class ReplacementHistoryResponse extends LinkedHashMap<String, Object> implements IResponse {
    public ReplacementHistoryResponse(Map<String, String> map) {
        putAll(map);
    }

    public ReplacementHistoryResponse(CoreMap map) {
        putAll(map.getInstance());
        putAll(map.getInstance2());
    }

    public void add(ReplacementHistoryResponse response) {
        if (get("changes") == null) {
            put("changes", new LinkedList<BoardResponse>());
        }
        ((LinkedList<ReplacementHistoryResponse>) get("changes")).add(response);
    }

    public static ReplacementHistoryResponse of(CoreMap map) {
        return new ReplacementHistoryResponse(map);
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
