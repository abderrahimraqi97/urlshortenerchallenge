package url.shortener.challenge.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String alias) {
        super("Alias already exists: " + alias);
    }
}
