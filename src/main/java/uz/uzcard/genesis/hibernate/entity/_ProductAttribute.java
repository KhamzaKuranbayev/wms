package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "product_attribute")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _ProductAttribute extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String state = "NEW";

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Product product;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Attribute attribute;

//    @Basic(fetch = FetchType.LAZY)
//    @ElementCollection
//    @CollectionTable(name = "_product_attribute_items", joinColumns = @JoinColumn(name = "_product_attribute_id"))
//    @Column(name = "items")
//    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    private List<String> items2 = new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb DEFAULT '[]'::jsonb", nullable = false)
    private List<String> items = new ArrayList<>();

    @Embedded
    private _AuditInfo auditInfo;

    public Long getId() {
        return id;
    }

    public _Product getProduct() {
        return _Entity.getLazyColumn(product);
    }

    public _Attribute getAttribute() {
        return _Entity.getLazyColumn(attribute);
    }
}
