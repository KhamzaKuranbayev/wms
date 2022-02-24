package uz.uzcard.genesis.dto.api.resp;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.hibernate.base.Callback;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.base._Entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class ListResponse extends Response {

    private int total;
    private Collection data;

    private ListResponse(Collection data, int total) {
        this.data = data;
        this.total = total;
    }

    private ListResponse(Collection data) {
        this.data = data;
        total = data.size();
    }

    public static <T extends _Entity> ListResponse of(PageStream<T> pageStream, Callback<T> callback) {
        return ListResponse.of(pageStream.stream().map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    CoreMap coreMap = callback.execute(t, t.getMap());
                    map.putAll(coreMap.getInstance());
                    map.putAll(coreMap.getInstance2());
                    return map;
                }).collect(Collectors.toList()),
                pageStream.getSize());
    }

    public static <T extends _Entity> ListResponse of(Stream<T> stream, int total, Callback<T> callback) {
        return ListResponse.of(stream.map(t -> {
            Map<String, Object> map = new HashMap<>();
            CoreMap coreMap = callback.execute(t, t.getMap());
            map.putAll(coreMap.getInstance());
            map.putAll(coreMap.getInstance2());
            return map;
        }).collect(Collectors.toList()), total);
    }

    public static ListResponse of(Collection data) {
        return new ListResponse(data);
    }

    public static ListResponse of(Collection data, int total) {
        return new ListResponse(data, total);
    }

    public boolean isSuccess() {
        return errors == null || errors.isEmpty();
    }
}