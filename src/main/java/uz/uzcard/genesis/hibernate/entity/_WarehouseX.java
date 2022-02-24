package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "warehouse_x")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
public class _WarehouseX extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private _Warehouse warehouse;

    @Column(name = "column_name")
    private String column;
    @Column(name = "sort_order")
    private Integer sortOrder;
    private Double width;

    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "column")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @OrderBy(value = "row_number")
    private List<_WarehouseY> rows = new ArrayList<>();

    public _WarehouseX(_Warehouse warehouse, String column, Integer sortOrder) {
        this.warehouse = warehouse;
        this.column = column;
        this.sortOrder = sortOrder;
    }

    @NumericField
    @Field(name = "id", analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}