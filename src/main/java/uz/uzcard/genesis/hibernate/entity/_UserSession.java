package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;
import java.time.Instant;

/**
 * Created by Virus on 19-Sep-16.
 */
@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "user_session")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class _UserSession extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";

    @org.hibernate.annotations.Index(name = "Index_user_session_session_id", columnNames = "session_id")
    @Column(unique = true, length = 1000)
    private String token;
    private String userAgent;
    private String IPAddress;
    @Column(columnDefinition = "boolean default false")
    private boolean expired = false;
    private Instant lastAccessTime;
    private Instant signiinDate;
    private String uuid;

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @org.hibernate.annotations.ForeignKey(name = "none")
    private _User user;
    @Column(insertable = false, updatable = false)
    private Long user_id;

}
