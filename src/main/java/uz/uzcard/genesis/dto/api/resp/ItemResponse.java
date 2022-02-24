package uz.uzcard.genesis.dto.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse extends Response {

    private Map<String, String> maps;

    private Map<String, List<String>> listMaps = new LinkedHashMap<>();
}
