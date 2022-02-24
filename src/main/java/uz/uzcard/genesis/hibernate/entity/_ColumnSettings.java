package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.EnumBridge;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.TableType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Indexed
@Getter
@Setter
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "column_settings")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _ColumnSettings extends _Entity {
    @DocumentId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = EnumBridge.class)
    @Enumerated(EnumType.STRING)
    private TableType tableType;

    private String columnName;
    private String columnLabel;
    private String minWidth;
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    private boolean visible;
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    private boolean enable;
    private boolean sortable;

    @Field(analyze = Analyze.NO, store = Store.NO)
    @NumericField
    @SortableField(forField = "position")
    private int position;
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    private boolean custom;

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User user;

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    @Basic(fetch = FetchType.LAZY)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "column_settings_role",
            joinColumns = {@JoinColumn(name = "settings_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    @ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_Role> roles = new ArrayList<>();


}