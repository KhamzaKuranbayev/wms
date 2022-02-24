package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StateRequest implements Serializable {
    private Long id;
    private String name;
    private String code;
    private String entityName;
    private int sortOrder = 0;
    private String description;
    private String colour;
}