package uz.uzcard.genesis.dto.backend;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PreviousChangesRequest implements Serializable {

    private Long objectId;
    private Integer revision;
}
