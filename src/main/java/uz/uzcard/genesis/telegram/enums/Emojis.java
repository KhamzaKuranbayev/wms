package uz.uzcard.genesis.telegram.enums;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

/**
 * @author Javohir Elmurodov
 * @created 1/14/2021 | 9:13 AM
 * @project GTL
 */

@AllArgsConstructor
public enum Emojis {

    ID(EmojiParser.parseToUnicode(":id:")),
    LABEL(EmojiParser.parseToUnicode(":label:")),
    KEY(EmojiParser.parseToUnicode(":key:")),
    RED_CIRCLE(EmojiParser.parseToUnicode(":red_circle:")),
    CALENDAR(EmojiParser.parseToUnicode(":calendar:")),
    PACKAGE(EmojiParser.parseToUnicode(":package:"));

    private String emojiName;

    @Override
    public String toString() {
        return emojiName;
    }
}
