package url.shortener.challenge.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import url.shortener.challenge.config.AppProperties;
import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import url.shortener.challenge.entity.Url;
import url.shortener.challenge.exception.AliasAlreadyExistsException;
import url.shortener.challenge.exception.UrlNotFoundException;
import url.shortener.challenge.repository.UrlRepository;
import url.shortener.challenge.service.UrlService;

import java.time.Instant;
import java.util.Optional;

@Service
public class UrlServiceImpl implements UrlService {

    private final UrlRepository repo;
    private final AppProperties props;
    private final StringRedisTemplate redis;

    public UrlServiceImpl(UrlRepository repo, AppProperties props, StringRedisTemplate redis) {
        this.repo = repo;
        this.props = props;
        this.redis = redis;
    }

    /**
     * Generates and stores a short code for the given long URL.
     *
     * @param req long URL request
     * @return response containing the short code
     */
    @Override
    public ShortUrlResponseDto create(LongUrlRequestDto req) {
        // generate NanoID with configurable length
        String code = NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET,
                props.getCodeLength()
        );

        if (repo.existsByShortUrl(code)) {
            throw new AliasAlreadyExistsException(code);
        }

        long ttl = props.getDefaultTtlSeconds();
        Instant expiresAt = Instant.now().plusSeconds(ttl);

        Url entity = new Url();
        entity.setShortUrl(code);
        entity.setLongUrl(req.getLongUrl());
        entity.setExpiresAt(expiresAt);

        try {
            repo.save(entity);
        } catch (DuplicateKeyException e) {
            // retry with a longer code if collision happens
            code = NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    NanoIdUtils.DEFAULT_ALPHABET,
                    props.getCodeLength() + 1
            );
            entity.setShortUrl(code);
            repo.save(entity);
        }

        cachePut(code, entity.getLongUrl(), ttl);
        return new ShortUrlResponseDto(entity.getShortUrl());
    }

    /**
     * Resolves a short URL identifier to its original long URL.
     *
     * @param shortUrl short URL identifier (hash code)
     * @return response containing the long URL
     * @throws UrlNotFoundException if no URL is found for this code
     */
    @Override
    public Optional<String> resolve(String shortUrl) {
        String cached = redis.opsForValue().get(cacheKey(shortUrl));
        if (cached != null) {
            incrementHitAsync(shortUrl);
            return Optional.of(cached);
        }

        return repo.findByShortUrl(shortUrl)
                .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(Instant.now()))
                .map(m -> {
                    long ttl = (m.getExpiresAt() != null)
                            ? Math.max(0, m.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond())
                            : 0;
                    cachePut(shortUrl, m.getLongUrl(), ttl);
                    incrementHitAsync(shortUrl);
                    return m.getLongUrl();
                });
    }

    private String cacheKey(String shortUrl) {
        return "url:" + shortUrl;
    }

    /**
     * Stores a short URL mapping in Redis with an optional TTL.
     */
    private void cachePut(String shortUrl, String longUrl, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redis.opsForValue().set(cacheKey(shortUrl), longUrl, java.time.Duration.ofSeconds(ttlSeconds));
        } else {
            redis.opsForValue().set(cacheKey(shortUrl), longUrl);
        }
    }

    /**
     * Asynchronously increments the hit count for the given short URL.
     */
    @Async
    void incrementHitAsync(String shortUrl) {
        repo.findByShortUrl(shortUrl).ifPresent(m -> {
            m.setHitCount(m.getHitCount() + 1);
            repo.save(m);
        });
    }
}
