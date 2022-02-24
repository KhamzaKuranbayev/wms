package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;

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
@Table(name = "contract_item")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _ContractItem extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SortableField
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    /**
     * Count - Zayavka berilgandagi mahsulot soni
     */
    @SortableField
    @NumericField
    @Field(analyze = Analyze.NO, store = Store.NO)
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double count;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double acceptedCount;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double rejectedCount;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double receipt;

    /**
     * Unit Type - Mahsulot o'lchov birligi
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private _UnitType unitType;

    /**
     * Contract Item Number
     */
    @SortableField
    @NumericField
    @Field(analyze = Analyze.YES, store = Store.NO)
//    @FieldBridge(impl = IntegerBridge.class)
    @Column(columnDefinition = "integer DEFAULT 0")
    private int numb = 0;

    /**
     * Contract
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Contract parent;

    /**
     * Product
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    /**
     * Product Group
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductGroup productGroup;

    /**
     * AcceptedUser
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _User acceptedUser;

    /**
     * Product Type - Mahsulot turi
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ProductType productType;

    /**
     * Guess Receive Date - Tahminiy yetkazib berish sanasi
     */
    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date itemGuessReceiveDate;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date actualReceiveDate;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date acceptedDate;

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @Column(name = "hash_esign", columnDefinition = "TEXT")
    private String hashESign;

    @Column(columnDefinition = "integer DEFAULT 0")
    private int initiatorCount;
    @Column(columnDefinition = "integer DEFAULT 0")
    private int initiatorAcceptedCount;

    @IndexedEmbedded(depth = 0, includePaths = {"parent.numb", "parent.id"})
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contractItem")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_OrderItem> orderItems = new ArrayList<>();

    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contractItem")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    private List<_UserAgreement> userAgreements = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "contract_item_files",
            joinColumns = {@JoinColumn(name = "contract_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "attachment_id")}
    )
    @Where(clause = "state != 'DELETED'")
    private List<_AttachmentView> resources = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "contract_item_akt_files",
            joinColumns = {@JoinColumn(name = "contract_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "attachment_id")}
    )
    @Where(clause = "state != 'DELETED'")
    private List<_AttachmentView> aktFiles = new ArrayList<>();

    @IndexedEmbedded(depth = 1, includePaths = {"givens.orderItem.parent.numb",
            "givens.orderItem.contractItem.parent.code", "lots.id", "lots.qrPrinted"}, indexNullAs = "null")
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contractItem")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    @OrderBy(value = "id")
    private List<_Partition> partitions = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private WarehouseReceivedType warehouseReceivedType;

    @NumericField
    @SortableField
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    public Double getRemains() {
        return count - acceptedCount - rejectedCount;
    }
}