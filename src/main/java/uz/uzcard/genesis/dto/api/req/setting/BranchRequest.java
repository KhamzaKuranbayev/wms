package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BranchRequest implements Serializable {
    private String mfo;
    private String name;
    private String parentMfo;
}