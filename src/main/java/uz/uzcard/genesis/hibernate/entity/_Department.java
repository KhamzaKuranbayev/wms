package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.bridge.builtin.LongBridge;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@Table(name = "department")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class _Department extends _Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Facet(name = "nameEn")
    @Field(analyze = Analyze.NO, index = Index.YES, name = "nameEn")
    @Column(name = "nameEn")
    private String nameEn;

    @Facet(name = "nameUz")
    @Field(analyze = Analyze.NO, index = Index.YES, name = "nameUz")
    @Column(name = "nameUz")
    private String nameUz;

    @Facet(name = "nameRu")
    @Field(analyze = Analyze.NO, index = Index.YES, name = "nameRu")
    @Column(name = "nameRu")
    private String nameRu;

    @Facet(name = "nameCyrl")
    @Field(analyze = Analyze.NO, index = Index.YES, name = "nameCyrl")
    @Column(name = "nameCyrl")
    private String nameCyrl;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Department parent;

    @Basic(fetch = FetchType.LAZY)
    @OneToMany(fetch = FetchType.LAZY)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Where(clause = "state != 'DELETED'")
    private Set<_Warehouse> warehouses = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderClassification depType = OrderClassification.DEPARTMENT;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "department_teams",
            joinColumns = {@JoinColumn(name = "department_id")},
            inverseJoinColumns = {@JoinColumn(name = "team_id")}
    )
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_Team> teams = new ArrayList<>();

    @Embedded
    private _AuditInfo auditInfo;

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @Field(name = "pr_for_id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getForPIId() {
        return id;
    }

    @IndexedEmbedded(depth = 0, includePaths = {"id"}, indexNullAs = "null")
    public _Department getParent() {
        return getLazyColumn(parent);
    }

    public String getNameByLanguage() {
        String language = SessionUtils.getInstance().getLanguage();
        switch (language) {
            case "en":
                return nameEn;
            case "ru":
                return nameRu;
            case "uz":
                return nameUz;
            default:
                return nameUz;
        }
    }
}