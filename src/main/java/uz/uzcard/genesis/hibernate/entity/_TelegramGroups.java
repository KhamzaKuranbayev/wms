package uz.uzcard.genesis.hibernate.entity;

import lombok.Getter;
import lombok.Setter;
import uz.uzcard.genesis.hibernate.base._Entity;

import javax.persistence.*;

/**
 * @author Javohir Elmurodov
 * @created 1/13/2021 | 4:16 PM
 * @project GTL
 */
@Getter
@Setter
@Entity
@Table(name = "telegramgroups")
public class _TelegramGroups extends _Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String state = "NEW";

    private boolean enabled;

    private String groupName;

    private Long chatId;

    private Long departmentId;
}
