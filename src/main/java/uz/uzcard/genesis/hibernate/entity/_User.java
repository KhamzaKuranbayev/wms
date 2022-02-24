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
import org.hibernate.search.bridge.builtin.LongBridge;
import org.hibernate.search.bridge.builtin.StringBridge;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.Index;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@Table(name = "users",
        indexes = {@Index(name = "Index_user_phone_and_actived", columnList = "phone, actived, state"),
                @Index(name = "Index_user_phone", columnList = "phone, state")}
)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class _User extends _Entity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    private String password;
    @Column(columnDefinition = "boolean default false")
    private boolean actived;
    @Column(columnDefinition = "boolean default false")
    private boolean locked;
    @Column(columnDefinition = "boolean default false")
    private boolean sysAdmin;
    @Column(columnDefinition = "boolean default false")
    private boolean leader;

    @Field(analyze = Analyze.YES)
    private String firstName;
    @Field(analyze = Analyze.YES)
    private String lastName;
    @Field(analyze = Analyze.YES)
    private String middleName;


    @Fields(value = {@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = StringBridge.class)),
            @Field(analyze = Analyze.NO, name = "userNameSort", index = org.hibernate.search.annotations.Index.NO)})
    @SortableField(forField = "userNameSort")
    @Column(unique = true)
    private String userName;

    //    @Column(unique = true)
    @Field(analyze = Analyze.YES)
    private String phone;
    //    @Column(unique = true)
    @Field(analyze = Analyze.YES)
    private String email;

    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @ManyToOne(fetch = FetchType.LAZY)
    private _Department department;

    @Basic(fetch = FetchType.LAZY)
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "user_role",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    @org.hibernate.annotations.ForeignKey(name = "none")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<_Role> roles = new ArrayList<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    private boolean phoneConfirmed;
    private Integer status;
    @Column(columnDefinition = "integer DEFAULT 0")
    private int attempt;


    @Embedded
    private _AuditInfo auditInfo;

    public _User(String firstName, String lastName, String middleName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.phone = phone;
    }

    @Facet(encoding = FacetEncodingType.STRING)
    @Field(store = Store.NO, analyze = Analyze.NO)
    public String getShortName() {
        return String.format("%s %s",
                StringUtils.isEmpty(lastName) ? "" : lastName,
                StringUtils.isEmpty(firstName) ? "" : firstName.charAt(0));
    }

    public String getFullName() {
        return String.format("%s %s %s",
                StringUtils.isEmpty(lastName) ? "" : lastName,
                StringUtils.isEmpty(firstName) ? "" : firstName,
                StringUtils.isEmpty(middleName) ? "" : middleName);
    }

    public SelectItem getSelectItem() {
        return new SelectItem(getId(), getLastName() + " " + getFirstName(), "" + getId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>(getRoles());
        roles.forEach(role -> role.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getCode()))));
        return authorities;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actived;
    }

    //    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(name = "id", analyze = Analyze.NO)
    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }

    @Facet(encoding = FacetEncodingType.STRING, name = "id2")
    @Field(analyze = Analyze.NO)
    public String getId2() {
        return "" + getId();
    }

    @IndexedEmbedded(depth = 0, indexNullAs = "null", includePaths = {"state", "pr_for_id","parent.id"})
    public _Department getDepartment() {
        return getLazyColumn(department);
    }
}