package uz.uzcard.genesis.dto.api.req;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.TableType;

import java.io.Serializable;

@Getter
@Setter
public class ColumnSettingsSortRequest implements Serializable {
    private TableType table;
    private String columnName;
    private int index;
}