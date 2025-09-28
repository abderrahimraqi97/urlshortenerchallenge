package url.shortener.challenge.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import url.shortener.challenge.entity.Url;

import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {
    Optional<Url> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
}
