package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
public class PermissionResponse extends LinkedHashMap<String, Object> implements IResponse {
    public PermissionResponse(Map<String, String> map) {
        putAll(map);
    }

    public void addRoles(List<String> roles) {
        Map<String, String[]> map = new LinkedHashMap<>();
        map.put("roles", roles.toArray(new String[]{}));
        putAll(map);
    }

    public void add(PermissionResponse response) {
        if (get("items") == null) {
            put("items", new LinkedList<PermissionResponse>());
        }
        ((LinkedList<PermissionResponse>) get("items")).add(response);
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