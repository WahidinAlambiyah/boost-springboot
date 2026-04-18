package id.allobank.exchangerate.strategy;

import id.allobank.exchangerate.model.dto.LatestRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LatestRatesStrategy implements IDRDataFetcher{

    private final WebClient webClient;

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

            Double usdRate = response.getRates().get("USD");

            if (usdRate == null) {
                throw new RuntimeException("USD rate not found");
            }

            double spread = calculateSpread("yourgithubusername");

            double result = (1 / usdRate) * (1 + spread);

            response.setUSD_BuySpread_IDR(result);

            return response;
//        } catch (Exception e) {
//            return Map.of(
//                    "error", "Failed to fetch latest rates",
//                    "message", e.getMessage()
//            );
//        }

    }

    private double calculateSpread(String username) {
        int sum = username.chars().sum();
        return (sum % 1000) / 100000.0;
    }
}
