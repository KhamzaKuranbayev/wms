package uz.uzcard.genesis.dto.api.resp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class PdfImageResponse implements Serializable {
    private String fileName;
    private int page;
    private int limit;
    private int total;
    private String mimeType;
    private List<String> base64s;
}