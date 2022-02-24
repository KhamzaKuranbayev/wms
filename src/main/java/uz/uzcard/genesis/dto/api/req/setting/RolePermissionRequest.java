package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RolePermissionRequest implements Serializable {
    private String role;
    private String permission;
    private boolean add;
}