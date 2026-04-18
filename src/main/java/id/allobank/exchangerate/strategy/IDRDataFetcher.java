package id.allobank.exchangerate.strategy;

public interface IDRDataFetcher {
    String getType();
    Object fetch();
}
