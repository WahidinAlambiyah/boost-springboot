package id.allobank.exchangerate.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class FrankfurterClientFactory implements FactoryBean<WebClient>{

    private static final Logger log = LoggerFactory.getLogger(FrankfurterClientFactory.class);
    @Value("${frankfurter.base-url}")
    private String baseUrl;

    @Override
    public WebClient getObject() throws Exception {
        log.info("WebClient initialized with baseUrl={}", baseUrl);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // connect timeout
                .responseTimeout(Duration.ofSeconds(5))              // response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

//        return WebClient.builder()
//                .baseUrl(baseUrl)
//                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

}