package url.shortener.challenge.constants;
public enum ErrorMessages {

    LONG_URL_REQUIRED(100,"Long URL is required"),
    LONG_URL_INVALID(101,"Must start with http:// or https://"),
    URL_NOT_FOUND(102,"URL not found for shortUrl: "),
    ALIAS_EXISTS(102, "Alias already exists: ");

    private Integer code;
    private final String message;

    ErrorMessages(Integer code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public Integer getCode() {
        return code;
    }
}
