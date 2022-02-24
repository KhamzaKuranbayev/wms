package uz.uzcard.genesis.dto.api.resp;

import java.io.Serializable;
import java.util.Map;

public interface IResponse extends Serializable {
    boolean isSuccess();

    int getCode();

    Map<String, String> getErrors();

    IResponse add(String key, String value);
}
