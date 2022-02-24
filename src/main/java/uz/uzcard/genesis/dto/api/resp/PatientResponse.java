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
public class PatientResponse extends LinkedHashMap<String, Object> implements IResponse{

    public PatientResponse(Map<String, String> map) {
        putAll(map);
    }

    public void add(PatientResponse response) {
        if (get("histories") == null) {
            put("histories", new LinkedList<PatientResponse>());
        }
        ((LinkedList<PatientResponse>) get("histories")).add(response);
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
