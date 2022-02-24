package uz.uzcard.genesis.config;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Created by norboboyev_h  on 06.07.2020  16:34
 */
@Configuration
public class FirebaseInitializer {
    @Value("${app.firebase-config}")
    private String firebaseConfig;

    @PostConstruct
    private void initialize() {
        FirebaseOptions options = null;
//        System.setProperty("https.proxyHost", "172.17.9.31");
//        System.setProperty("https.proxyPort", "8080");
//        System.setProperty("com.google.api.client.should_use_proxy", "true");
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("172.17.9.31", 8080));
//        HttpTransport httpTransport = new NetHttpTransport.Builder().setProxy(proxy).build();
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(firebaseConfig)
                            .getInputStream())).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseApp firebaseApp;
        if (FirebaseApp.getApps().isEmpty()) {
            firebaseApp = FirebaseApp.initializeApp(options);
        } else {
            firebaseApp = FirebaseApp.getInstance();
        }
    }
}
