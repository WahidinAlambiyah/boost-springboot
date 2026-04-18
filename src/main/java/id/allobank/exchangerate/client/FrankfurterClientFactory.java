package id.allobank.exchangerate.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class FrankfurterClientFactory implements FactoryBean<WebClient>{

    @Value("${frankfurter.base-url}")
    private String baseUrl;

    @Override
    public WebClient getObject() throws Exception {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // connect timeout
                .responseTimeout(Duration.ofSeconds(5))              // response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5)));

        return WebClient.builder()
                .baseUrl(baseUrl)
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