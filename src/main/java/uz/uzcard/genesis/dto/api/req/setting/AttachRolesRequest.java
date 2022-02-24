package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Created by norboboyev_h  on 29.07.2020  14:21
 */
@Getter
@Setter
public class AttachRolesRequest {
    private Set<Long> roles;
    private Long userId;
}
