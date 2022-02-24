package uz.uzcard.genesis.uitls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uz.uzcard.genesis.config.ApplicationContextProvider;
import uz.uzcard.genesis.exception.ValidatorException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;


public class ApiConnector {
    private static final Logger log = LogManager.getLogger(ApiConnector.class);

    public static Builder newBuilder(Class<?> responseType) {
        return new ApiConnector().new Builder(responseType);
    }

    public class Builder<T> {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        RestTemplate restTemplate = ApplicationContextProvider.applicationContext.getBean(RestTemplate.class);
        private final Map params = new ConcurrentHashMap();
        private ResponseEntity<T> result;
        private final Class<T> responseType;
        private String url;

        public Builder(Class<T> responseType) {
            this.responseType = responseType;
            this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
                    return httpResponse.getStatusCode().series() == CLIENT_ERROR
                            || httpResponse.getStatusCode().series() == SERVER_ERROR;
                }

                @Override
                public void handleError(ClientHttpResponse httpResponse) throws IOException {
                    if (httpResponse.getStatusCode()
                            .series() == HttpStatus.Series.SERVER_ERROR) {
                        // handle SERVER_ERROR
                    } else if (httpResponse.getStatusCode()
                            .series() == HttpStatus.Series.CLIENT_ERROR) {
                        // handle CLIENT_ERROR
                        if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                            throw new ValidatorException("URL топилмади");
                        }
                    }
                }
            });
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder clearParams() {
            params.clear();
            return this;
        }

        public Builder addParam(String key, Object value) {
            if (value == null || key == null) return this;
            this.params.put(key, value);
            return this;
        }

        public Builder addHeader(String key, String value) {
            headers.add(key, value);
            return this;
        }

        public Builder post() {
//            result = restTemplate.postForEntity(url, params, responseType);
//            return this;
            return exchange(HttpMethod.POST);
        }

        public Builder post(Object data) {
            HttpEntity<Map> request = getMapHttpEntity(data);
            result = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
            return this;
        }

        public Builder post(Map<String, Object> data) {
            if (data != null)
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue() != null)
                        params.put(entry.getKey(), entry.getValue());
                }
            HttpEntity<Map> request = new HttpEntity<Map>(params, headers);
            result = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
            return this;
        }

        public Builder put(Object data) {
            HttpEntity<Map> request = getMapHttpEntity(data);
            result = restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
            return this;
        }

        private HttpEntity<Map> getMapHttpEntity(Object data) {
            Map<String, Object> params = new HashMap<>();
            if (data != null)
                for (Field field : FieldUtil.getDeclaredFields(data.getClass())) {
                    field.setAccessible(true);
                    try {
                        params.put(field.getName(), field.get(data));
                    } catch (IllegalAccessException e) {
                        //e.printStackTrace();
                        ServerUtils.error(log, e);
                        throw new ValidatorException("Серверда хатолик юз берди");
                    }
                }
            return new HttpEntity<Map>(params, headers);
        }

        public Builder exchange(HttpMethod httpMethod) {
            HttpEntity<Map> request = new HttpEntity<Map>(params, headers);
            result = restTemplate.exchange(url, httpMethod, request, responseType);
            return this;
        }

        public Builder get() {
//            result = restTemplate.getForEntity(url, responseType, params);
//            return this;
            return exchange(HttpMethod.GET);
        }

        public T build() {
            return result.getBody();
        }

        public Builder delete() {
            return exchange(HttpMethod.DELETE);
        }
    }
}