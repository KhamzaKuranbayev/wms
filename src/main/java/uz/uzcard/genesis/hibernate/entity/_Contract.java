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
import org.hibernate.search.bridge.builtin.DoubleBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.hibernate.search.bridge.builtin.StringBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.SupplyType;

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
@Table(name = "contracts")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//@AnalyzerDef(name = "searchTextAnalyzer",
//        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
//        filters = {@TokenFilterDef(factory = LowerCaseFilterFactory.class), @TokenFilterDef(factory = ASCIIFoldingFilterFactory.class)})
public class _Contract extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SortableField
    @Field(analyze = Analyze.NO)
    private String state = "CONTRACT_CONCLUTION";
    @Fields(value = {@Field(analyze = Analyze.YES, index = Index.YES, bridge = @FieldBridge(impl = StringBridge.class)),
            @Field(analyze = Analyze.NO, name = "codeSort", index = org.hibernate.search.annotations.Index.NO)})
    @SortableField(forField = "codeSort")
    @Column(unique = true)
    private String code;

    /**
     * Guess Receive Date - Tahminiy yetkazib berish sanasi
     * Actual Receive Date - Yetkazib berilgan sanasi
     * Conclusion Date - Заключен контракт date - Shartnoma imzolangan sanasi
     */
    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date guessReceiveDate;

    /**
     * completedDate - Contractni barcha itemlari qabul qilinsa kegin new Date() saqlanadi;
     */
    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date completedDate;

    @SortableField
    @Field(index = Index.YES, analyze = Analyze.YES)
    @DateBridge(resolution = Resolution.MILLISECOND, encoding = EncodingType.STRING)
    @Column
    private Date conclusionDate;

    /**
     * Reject Reason - rad etish sababi
     */
    @Column
    private String rejectReason;

    /**
     * Contract Item List - Kontract itemlari(Childlari)
     */
    @IndexedEmbedded(depth = 0, includePaths = {"orderItems.parent.numb", "orderItems.state",
            "userAgreements.user.id", "userAgreements.notificationSent", "userAgreements.statusType",
            "productGroup.id", "productType.id", "product.name", "userAgreements.initiatorInfo", "state"},
            indexNullAs = "null")
    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    private List<_ContractItem> items = new ArrayList<>();

    /**
     * Product Resource File
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView productResource;

    /**
     * соглашения
     */
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "contract_agreement_files",
            joinColumns = {@JoinColumn(name = "contract_id")},
            inverseJoinColumns = {@JoinColumn(name = "attachment_id")}
    )
    @Where(clause = "state != 'DELETED'")
    private List<_AttachmentView> agreementResources = new ArrayList<>();

    /**
     * Reject Resource File
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _AttachmentView rejectResource;

    /**
     * Supplier - Yetkazib beruvchi
     */
    @IndexedEmbedded(depth = 1)
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Supplier supplier;

    /**
     * Supply Type - Yetkazib berish turi
     */
    @SortableField
    @Field(analyze = Analyze.NO)
    @Enumerated(EnumType.STRING)
    private SupplyType supplyType;

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    private Integer acceptCount;
    private Integer totalCount;
    @Column(name = "default_yearly")
    private Integer defaultYearly;

    @NumericField
    @SortableField
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    public _AttachmentView getProductResource() {
        return getLazyColumn(productResource);
    }

    public _AttachmentView getRejectResource() {
        return getLazyColumn(rejectResource);
    }

    public _Supplier getSupplier() {
        return getLazyColumn(supplier);
    }

    @Field(analyze = Analyze.NO, name = "diffDate", indexNullAs = "null")
    @FieldBridge(impl = LongBridge.class)
    public Long getDiffDate() {
        if (guessReceiveDate == null)
            return null;
        if (completedDate != null) {
            return guessReceiveDate.getTime() - completedDate.getTime();
        } else {
            return guessReceiveDate.getTime() - new Date().getTime();
        }
    }

    @Facet(name = "creationDateFacetDay")
    @Field(name = "creationDateFacetDay", analyze = Analyze.NO, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    public Date getCreationDateFacetDay() {
        if (conclusionDate != null)
            return conclusionDate;
        else return null;
    }

    @Facet(name = "creationDateFacetMonth")
    @Field(name = "creationDateFacetMonth", analyze = Analyze.NO, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MONTH, encoding = EncodingType.STRING)
    public Date getCreationDateFacetMonth() {
        if (conclusionDate != null)
            return conclusionDate;
        else return null;
    }

    @Facet(name = "completedDateFacetDay")
    @Field(name = "completedDateFacetDay", analyze = Analyze.NO, indexNullAs = "null")
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    public Date getCompletedDateFacetDay() {
        return completedDate;
    }

    @Facet(name = "completedDateFacetMonth")
    @Field(name = "completedDateFacetMonth", analyze = Analyze.NO, indexNullAs = "null")
    @DateBridge(resolution = Resolution.MONTH, encoding = EncodingType.STRING)
    public Date getCompletedDateFacetMonth() {
        return completedDate;
    }
}