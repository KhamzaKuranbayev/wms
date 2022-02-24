package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.DoubleBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base.StringCollectionFieldBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.PlacementType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Indexed(index = "product_item")
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "product_item",
        indexes = {
                @javax.persistence.Index(columnList = "qrcode"),
                @javax.persistence.Index(columnList = "lot_id, state"),
                @javax.persistence.Index(columnList = "partition_id, state")
        })
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _ProductItem extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES)
    private String name;

    @Field(analyze = Analyze.NO)
    private String accountingCode;

    private Double price;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Package packages;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Warehouse warehouse;

    @Field(analyze = Analyze.NO)
    @Enumerated(EnumType.STRING)
    private PlacementType placementType = null;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Partition partition;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Lot lot;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _User givenUser;

    @IndexedEmbedded(depth = 0, includePaths = {"id", "department.pr_for_id"})
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _User takenAwayUser;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _User placedBy;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date placedDate;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date takenAwayDate;

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(nullable = false, name = "is_qr_printed", columnDefinition = "boolean DEFAULT false")
    private boolean qrPrinted;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = StringCollectionFieldBridge.class)
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> cells_id = new ArrayList<>();

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = StringCollectionFieldBridge.class)
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> carriages_id = new ArrayList<>();

    @SortableField
    @NumericField
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DoubleBridge.class)
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double count;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _UnitType unitType;

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private _OrderItem orderItem;

    private Long qrcode;
    @Field(analyze = Analyze.NO)
    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean used;

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private _InventarizationLog inventarizationLog;

    /**
     * When product used by department then set data under fields
     */
    @Column(name = "comment")
    private String comment;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView resourceForUsed;

    public void setCount(double count) {
        this.count = count;
    }

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    @Override
    public Long getId() {
        return id;
    }

    @Field(analyze = Analyze.NO, name = "idSort", index = Index.NO)
    @NumericField(forField = "idSort")
    @SortableField(forField = "idSort")
    public Long getId2() {
        return id;
    }

    @IndexedEmbedded(depth = 1, includePaths = {"contractItem.orderItems.itemNumb",
            "contractItem.orderItems.parent.numb", "contractItem.numb", "contractItem.parent.code",
            "contractItem.parent.id"})
    public _Partition getPartition() {
        return getLazyColumn(partition);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"id", "name"})
    public _Lot getLot() {
        return getLazyColumn(lot);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"parent.numb", "parent.id", "itemNumb", "contractItem.parent.code"})
    public _OrderItem getOrderItem() {
        return getLazyColumn(orderItem);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"valid"}, indexNullAs = "null")
    public _InventarizationLog getInventarizationLog() {
        return getLazyColumn(inventarizationLog);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"id", "name"})
    public _Product getProduct() {
        return getLazyColumn(product);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"type.id2", "type.id"}, indexNullAs = "null")
    public _Package getPackages() {
        return getLazyColumn(packages);
    }

    @IndexedEmbedded(depth = 0, includePaths = {"id", "id2"})
    public _Warehouse getWarehouse() {
        return getLazyColumn(warehouse);
    }

    public _UnitType getUnitType() {
        return getLazyColumn(unitType);
    }

    //todo vremenno
    public List<Long> getCells_id() {
        Object temp = cells_id;
        if (cells_id == null)
            return Collections.emptyList();
        return (List<Long>) ((List) temp).stream().map(integer -> Long.parseLong("" + integer)).collect(Collectors.toList());
    }

    public void setCells_id(List<Long> cells_id) {
        this.cells_id = cells_id.stream().map(String::valueOf).collect(Collectors.toList());
    }

    //    todo vremenno
    public List<Long> getCarriages_id() {
        Object temp = carriages_id;
        if (carriages_id == null)
            return Collections.emptyList();
        return (List<Long>) ((List) temp).stream().map(integer -> Long.parseLong("" + integer)).collect(Collectors.toList());
    }

    public void setCarriages_id(List<Long> carriages_id) {
        this.carriages_id = carriages_id.stream().map(String::valueOf).collect(Collectors.toList());
    }

    @SortableField
    @Field(name = "inventarizationDate", analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    public Date getInventarizationDate() {
        if (getInventarizationLog() == null || getInventarizationLog().getInventarization() == null)
            return null;
        if (getInventarizationLog().getInventarization().getEndedAt() == null)
            return getInventarizationLog().getAuditInfo().getCreationDate();
        return getInventarizationLog().getInventarization().getEndedAt();
    }

    @Facet(encoding = FacetEncodingType.STRING)
    @Field(name = "takenAwayUserDepartment", analyze = Analyze.NO, indexNullAs = "null")
    public String getTakenAwayUserDepartment() {
        if (takenAwayUser != null) {
            if (takenAwayUser.getDepartment() != null) {
                return "" + takenAwayUser.getDepartment().getId();
            }
        }
        return null;
    }
}