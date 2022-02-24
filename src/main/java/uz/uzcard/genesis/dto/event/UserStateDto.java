package uz.uzcard.genesis.dto.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStateDto {

    private MessageType type;
    private String userName;
    private String content;

    public enum MessageType {
        LOGIN,
        LOGOUT,

    }
}
