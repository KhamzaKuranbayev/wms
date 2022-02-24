package uz.uzcard.genesis.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AttachmentDto implements Serializable {
    private String name;
    private byte[] bytes;
    private String mimeType;
    private long size;
    private List<String> pages = new ArrayList<>();

    public boolean isEmpty() {
        return bytes == null || bytes.length < 1;
    }

    public AttachmentDto(String name) {
        this.name = name;
    }

    public AttachmentDto(String name, byte[] bytes, String mimeType, long size) {
        this.name = name;
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.size = size;
    }
}