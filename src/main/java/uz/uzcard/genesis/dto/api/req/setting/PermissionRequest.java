package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PermissionRequest implements Serializable {
    private String code;
    private String name;
    private String parentCode;
}