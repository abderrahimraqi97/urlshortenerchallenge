package url.shortener.challenge.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortUrl) {
        super("URL not found for shortUrl: " + shortUrl);
    }
}
