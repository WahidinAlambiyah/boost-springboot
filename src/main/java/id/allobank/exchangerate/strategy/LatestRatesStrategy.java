package id.allobank.exchangerate.strategy;

import id.allobank.exchangerate.exception.ApiException;
import id.allobank.exchangerate.model.dto.LatestRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LatestRatesStrategy implements IDRDataFetcher {

    private final WebClient webClient;

    @Value("${app.github-username}")
    private String username;

    @Override
    public String getType() {
        return "latest_idr_rates";
    }

    @Override
    public Object fetch() {
//        try {
        LatestRatesResponse response = webClient.get()
                .uri("/latest?base=IDR")
                .retrieve()
                .onStatus(status -> status.isError(), r ->
                        Mono.error(new RuntimeException("API Error")))
                .bodyToMono(LatestRatesResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Null response from API");
        }

        if (response.getRates() == null) {
            throw new ApiException("Rates data missing");
        }

        Double usdRate = response.getRates().get("USD");
        log.info("USD Rate: {}", usdRate);

        if (usdRate == null) {
            throw new RuntimeException("USD rate not found");
        }

        double spread = calculateSpread(username);
        log.info("Spread: {}", spread);

        double result = (1 / usdRate) * (1 + spread);

        response.setUSD_BuySpread_IDR(result);

        return Map.of(
                "resourceType", getType(),
                "data", response,
                "fetchedAt", Instant.now().toString()
        );
//        } catch (Exception e) {
//            return Map.of(
//                    "error", "Failed to fetch latest rates",
//                    "message", e.getMessage()
//            );
//        }

    }

    private double calculateSpread(String username) {
        if (username == null || username.isBlank()) {
            throw new ApiException("Invalid GitHub username: must not be null or blank");
        }

        int sum = username.toLowerCase(Locale.ROOT).chars().sum();
        return (sum % 1000) / 100000.0;
    }
}
