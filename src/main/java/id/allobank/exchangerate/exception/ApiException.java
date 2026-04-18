package id.allobank.exchangerate.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}