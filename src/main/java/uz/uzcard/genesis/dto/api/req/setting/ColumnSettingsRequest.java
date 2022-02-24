package uz.uzcard.genesis.dto.api.req.setting;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.enums.TableType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ColumnSettingsRequest implements Serializable {
    private Long id;
    private TableType table;
    private String columnName;
    private String columnLabel;
    private String minWidth;
    private boolean visible;
    private boolean enable;
    private boolean sortable;
    private int position;
    private Set<Long> roleIds = new HashSet<>();
    private boolean general;
}