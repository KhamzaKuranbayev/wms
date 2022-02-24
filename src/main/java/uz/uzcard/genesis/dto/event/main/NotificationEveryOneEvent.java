package uz.uzcard.genesis.dto.event.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEveryOneEvent {

    private String title;
    private String comment;
    private List<String> userNames;
}
