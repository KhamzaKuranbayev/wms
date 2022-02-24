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
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.Date;

@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "lots")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Lot extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _Partition partition;

    @Field(analyze = Analyze.YES)
    private String name;

    private Integer packageCount;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double count;
    @Column(columnDefinition = "float DEFAULT 0", precision = 10, scale = 2)
    private double remains;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean qrPrinted;

    @Column(name = "act_no")
    private String actNo;

    @Column(name = "gtd")
    private String gtd;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private Date invoiceDate;

    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @IndexedEmbedded(depth = 0, includePaths = {"contractItem.id", "date"})
    public _Partition getPartition() {
        return getLazyColumn(partition);
    }
}