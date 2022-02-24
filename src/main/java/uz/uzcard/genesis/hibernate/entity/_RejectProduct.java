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
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "rejected_products")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
public class _RejectProduct extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SortableField
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double rejectedCount;
    private String description;

    @IndexedEmbedded(depth = 0, includePaths = {"creationDate"})
    @Embedded
    private _AuditInfo auditInfo;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _ContractItem contractItem;

    @IndexedEmbedded(depth = 0, includePaths = {"name", "nameFacet"})
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Supplier supplier;

    public _RejectProduct(_ContractItem contractItem, _Supplier supplier, double rejectedCount, String description) {
        this.rejectedCount = rejectedCount;
        this.contractItem = contractItem;
        this.supplier = supplier;
        this.description = description;
    }

    @IndexedEmbedded(depth = 2, includePaths = {"parent.id"})
    public _ContractItem getContractItem() {
        return getLazyColumn(contractItem);
    }
}