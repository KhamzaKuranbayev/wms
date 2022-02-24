package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.TableType;

import java.io.Serializable;

@Getter
@Setter
public class ColumnSettingsUpdateRequest implements Serializable {
    private String columnName;
    private Boolean visible;
    private String width;
    private TableType table;
}