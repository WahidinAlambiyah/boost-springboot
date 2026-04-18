package id.allobank.exchangerate.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CurrencyResponse {

    private Map<String, String> currencies;
}
