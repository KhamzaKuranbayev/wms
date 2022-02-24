package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.hibernate.base.Callback;
import uz.uzcard.genesis.hibernate.base._Entity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SingleResponse<T> extends Response {
    private T data;

    private SingleResponse(T data) {
        this.data = data;
        this.success = true;
    }

    private SingleResponse() {
        this.success = true;
    }

    public static <T> SingleResponse of(T data) {
        return new SingleResponse(data);
    }

    public static <T extends _Entity, H> SingleResponse of(T data, Callback<T> callback) {
        if (data == null) return empty();
        Map<String, Object> map = new HashMap<>();
        CoreMap coreMap = callback.execute(data, data.getMap(true));
        map.putAll(coreMap.getInstance());
        map.putAll(coreMap.getInstance2());
        return new SingleResponse(map);
    }

    public static <T> SingleResponse empty() {
        return new SingleResponse();
    }

    public boolean isSuccess() {
        return errors == null || errors.isEmpty();
    }
}