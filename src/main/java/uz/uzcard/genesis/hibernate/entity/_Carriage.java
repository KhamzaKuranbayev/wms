package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "carriage")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class _Carriage extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _StillageColumn stillageColumn;

    @Field(analyze = Analyze.NO)
    private int sortOrder;

    /**
     * O'lchov birligi sm
     */
    private Integer width;

    /**
     * O'lchov birligi sm
     */
    private Integer height;

    /**
     * O'lchov birligi sm
     */
    private Integer depth;

    private Integer remainedCapacityPercentage;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(name = "is_full", nullable = false)
    private boolean full;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(name = "has_product", nullable = false)
    private boolean hasProduct = false;

    @Embedded
    private _AuditInfo auditInfo;

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }

    @IndexedEmbedded(depth = 0, includePaths = {"id", "code"})
    public _StillageColumn getStillageColumn() {
        return getLazyColumn(stillageColumn);
    }
}