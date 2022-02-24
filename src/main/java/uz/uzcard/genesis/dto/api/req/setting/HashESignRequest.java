package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 01.10.2020  1:09
 */
@Getter
@Setter
public class HashESignRequest implements Serializable {
    private Long id;
    private String hashESign;
}
