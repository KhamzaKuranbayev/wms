package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.DoubleBridge;
import org.hibernate.search.bridge.builtin.IntegerBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "order_items")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _OrderItem extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Facet(name = "state")
    @Field(analyze = Analyze.NO, index = Index.YES, name = "state")
    private String state = "NEW";

    @IndexedEmbedded(depth = 0, includePaths = {"createdByUser.id", "creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _Department department;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Order parent;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column(name = "time_to_be_entered")
    private Date timeToBeEntered;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product offerProduct;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductGroup productGroup;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductType productType;

    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = DoubleBridge.class)
    private Double count;

    @Column(columnDefinition = "boolean DEFAULT true", nullable = false)
    private boolean seenInitiatorNotification;

    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = DoubleBridge.class)
    private Double offerCount;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private _UnitType unitType;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _UnitType offerUnitType;

    @Field(analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    private Date itemSendDate;
    private Date itemGuessReceiveDate;
    private Date itemReceiveDate;

    private String rejectionReason;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView rejectResource;

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"id", "firstName"})
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User statusChangedUser;

    @Fields(value = {@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = IntegerBridge.class)),
            @Field(analyze = Analyze.NO, name = "itemNumbSort", index = Index.NO)})
    @NumericField(forField = "itemNumbSort")
    @SortableField(forField = "itemNumbSort")
    @Column(columnDefinition = "integer DEFAULT 1")
    private Integer itemNumb = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _OrderItem relation;

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _ContractItem contractItem;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView offerAttachment;

    private String offerComment;

    @Column(name = "hash_esign", columnDefinition = "TEXT")
    private String hashESign;

    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = DoubleBridge.class)
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double bron = 0;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double given = 0;
    @Column(columnDefinition = "float DEFAULT 0", nullable = false)
    private double takenAway = 0;

    @IndexedEmbedded(depth = 0, includePaths = {"id", "state"})
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderItem")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state = 'NEW'")
    private List<_GivenProducts> givens = new ArrayList<>();

    /**
     * When _OrderItem status change to PENDING_PURCHASE from PAPER_EXPECTED_SPECIFICATION
     * then set LocalDate now();
     */
    @Field(analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @Column(name = "pending_purchase_date")
    private Date pendingPurchaseDate;

    /**
     * When _OrderItem status change to DELIVERY_EXPECTED
     * then set LocalDate now();
     */
    @Field(analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @Column(name = "contract_date")
    private Date contractDate;

    /**
     * _OrderItem last action date
     */
    @Field(analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @Column(name = "item_conclusion_date")
    private Date itemConclusionDate;

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @NumericField
    @Field(name = "id", analyze = Analyze.NO, store = Store.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @Field(analyze = Analyze.NO, name = "remains")
    @FieldBridge(impl = DoubleBridge.class)
    private Double getRemains() {
        return count - given;
    }

    public _AttachmentView getRejectResource() {
        return getLazyColumn(rejectResource);
    }

    public _OrderItem getRelation() {
        return getLazyColumn(relation);
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"parent.code", "parent.id", "state", "parent.supplier.id"})
    public _ContractItem getContractItem() {
        return getLazyColumn(contractItem);
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"id", "parent.id", "nameUz", "nameRu", "nameEn"})
    public _Department getDepartment() {
        return getLazyColumn(department);
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"sendDate", "numb", "id", "state"})
    public _Order getParent() {
        return getLazyColumn(parent);
    }

    @IndexedEmbedded(depth = 1)
    public _Product getProduct() {
        return getLazyColumn(product);
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"id"})
    public _ProductGroup getProductGroup() {
        return getLazyColumn(productGroup);
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"id", "nameFacet"})
    public _ProductType getProductType() {
        return getLazyColumn(productType);
    }

    public _UnitType getUnitType() {
        return getLazyColumn(unitType);
    }

    public _Product getOfferProduct() {
        return getLazyColumn(offerProduct);
    }
}