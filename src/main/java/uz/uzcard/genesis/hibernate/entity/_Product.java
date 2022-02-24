package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.IntegerBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base.StringCollectionFieldBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "product")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Product extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES)
    private String name;

    @Field(analyze = Analyze.YES, name = "name")
    private String uniqueKey;

    @Basic(fetch = FetchType.LAZY)
    @ElementCollection
    @CollectionTable(name = "_product_attr", joinColumns = @JoinColumn(name = "_product_id"))
    @Column(name = "attr")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<String> attr2 = new ArrayList<>();

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = StringCollectionFieldBridge.class)
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> attr = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductType type;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductGroup group;

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    @Basic(fetch = FetchType.LAZY)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "product_unit_types",
            joinColumns = {@JoinColumn(name = "product_id")},
            inverseJoinColumns = {@JoinColumn(name = "unit_type_id")}
    )
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_UnitType> unitTypes = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_ProductAttribute> attributes = new ArrayList<>();

    @Embedded
    private _AuditInfo auditInfo;
    @SortableField()
    @NumericField
    @Field(analyze = Analyze.NO)
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double remains = 0;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double bron = 0;

    /**
     * MSDS - Material Safety Data Sheet
     */
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView msds;
    //second
    private Long expiration;

    @Field(analyze = Analyze.NO)
    private Integer limitCount;

    /**
     * Limitga nisbatan qolgan mahsulot miqdorini foizi uchun kerak
     */
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = IntegerBridge.class)
    private Integer percentRemainingToLimit;

    //    @Fields(value = {@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class)),
//            @Field(analyze = Analyze.NO, name = "idSort", index = Index.NO)})
//    @NumericField(forField = "idSort")
//    @SortableField(forField = "idSort")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    public double getCount() {
        return remains - bron;
    }

    @IndexedEmbedded(depth = 1)
    public _AttachmentView getMsds() {
        return getLazyColumn(msds);
    }

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    public _ProductType getType() {
        return getLazyColumn(type);
    }

    @IndexedEmbedded(depth = 1, indexNullAs = "null")
    public _ProductGroup getGroup() {
        return getLazyColumn(group);
    }

    @SortableField(forField = "nameSort")
    @Field(name = "nameSort", index = Index.NO, analyze = Analyze.NO)
    public String getName() {
        return name;
    }

    @SortableField(forField = "remains_limit_count_diff")
    @NumericField
    @Field(analyze = Analyze.NO, name = "remains_limit_count_diff")
    public double getDiffRemainAndLimitCount() {
        if (limitCount != null)
            return remains - Double.valueOf(limitCount.toString());
        else
            return remains;
    }
}