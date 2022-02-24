package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;

/**
 * Created by norboboyev_h  on 07.07.2020  16:26
 */

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "fire_base",
        indexes = {@Index(name = "Index_fire_base_user_device_state", columnList = "user_id, device_id, state")})
public class _FirebaseToken extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(analyze = Analyze.NO)
    private String state = "NEW";


    @Column
    private String token;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private String deviceId;

    @Embedded
    private _AuditInfo auditInfo;

}
