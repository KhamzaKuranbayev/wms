package uz.uzcard.genesis.dto.api.resp;

import uz.uzcard.genesis.dto.CoreMap;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class TeamResponse extends LinkedHashMap<String, Object> implements Serializable {
    public TeamResponse(CoreMap map) {
        putAll(map.getInstance());
        putAll(map.getInstance2());
        remove("departments");
    }

    public void add(TeamResponse response) {
        if (get("departments") == null) {
            put("departments", new LinkedList<TeamResponse>());
        }
        ((LinkedList<TeamResponse>) get("departments")).add(response);
    }
}