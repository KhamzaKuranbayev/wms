package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.PlaceType;

import javax.persistence.*;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "warehouse_y")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
public class _WarehouseY extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private _WarehouseX column;

    @Field(analyze = Analyze.NO)
    @Column(name = "row_number")
    private Integer row;
    private Double height;
    private Double widht;
    @Enumerated(EnumType.STRING)
    private PlaceType placeType;

    public _WarehouseY(_WarehouseX column, Integer row, PlaceType placeType) {
        this.column = column;
        this.row = row;
        this.placeType = placeType;
    }

    @NumericField
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(analyze = Analyze.NO)
    public String getId2() {
        return "" + getId();
    }
}