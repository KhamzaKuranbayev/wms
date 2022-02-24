package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class HashESignsRequest implements Serializable {

    private List<Long> ids;
    private String hashESign;
}
