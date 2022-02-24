package uz.uzcard.genesis.hibernate.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.springframework.security.core.GrantedAuthority;
import uz.uzcard.genesis.hibernate.base._Item;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Virus on 25-Aug-16.
 */
@Indexed
@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "roles")
//@Where(clause = "state <> 2")
//@SQLDelete(sql = "UPDATE Role SET state = 2 WHERE id = ?")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _Role extends _Item implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(hidden = true)
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @Field(analyze = Analyze.YES)
    @Column(unique = true)
    private String code;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id")}
    )
    @Where(clause = "state != 'DELETED'")
    private List<_Permission> permissions = new ArrayList<>();

    @Override
    public String getAuthority() {
        return "ROLE_" + getCode();
    }

    @SortableField
    @NumericField
    @Field(name = "id", analyze = Analyze.NO)
//    @FieldBridge(impl = LongBridge.class)
    public Long getId() {
        return id;
    }
}