package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse extends LinkedHashMap<String, Object> implements IResponse {
    public OrderResponse(Map<String, String> map) {
        putAll(map);
    }

    public OrderResponse(ItemResponse map) {
        putAll(map.getMaps());
        putAll(map.getListMaps());
    }

    public void add(OrderResponse response) {
        if (get("items") == null) {
            put("items", new LinkedList<OrderResponse>());
        }
        ((LinkedList<OrderResponse>) get("items")).add(response);
    }

    public void addFile(FileResponse response) {
        if (get("attachments") == null) {
            put("attachments", new LinkedList<FileResponse>());
        }
        ((LinkedList<FileResponse>) get("attachments")).add(response);
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