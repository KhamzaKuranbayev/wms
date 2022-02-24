package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.dto.api.req.setting.NotificationDataReq;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.NotificationCategoryType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * Created by norboboyev_h  on 24.12.2020  10:54
 */
@Indexed
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "notification")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Notification extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @IndexedEmbedded(depth = 1)
    @Embedded
    private _AuditInfo auditInfo;

    @Field(analyze = Analyze.YES)
    private String body;

    @Field(analyze = Analyze.YES)
    private String title;

    @SortableField
    @Field(analyze = Analyze.NO)
    @Enumerated(EnumType.STRING)
    private NotificationCategoryType type;

    @IndexedEmbedded(depth = 0, includePaths = {"id", "id2"})
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ForeignKey(name = "none")
    private _User sentTo;

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "jsonb")
    private NotificationDataReq data;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = BooleanBridge.class)
    @Column(columnDefinition = "boolean DEFAULT false", name = "is_read", nullable = false)
    private boolean read;

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}
