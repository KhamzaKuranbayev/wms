package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by norboboyev_h  on 02.07.2020  17:54
 */
@Getter
@Setter
public class ConfirmationCodeRequest implements Serializable {

    private String code;
    private String username;
}
