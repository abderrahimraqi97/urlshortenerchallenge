package url.shortener.challenge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ResolveUrlResponseDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import url.shortener.challenge.exception.UrlNotFoundException;
import url.shortener.challenge.service.UrlService;
import url.shortener.challenge.util.RateLimiter;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private static final Logger log = LoggerFactory.getLogger(UrlController.class);

    private final UrlService service;
    private final RateLimiter limiter;

    public UrlController(UrlService service, RateLimiter limiter) {
        this.service = service;
        this.limiter = limiter;
    }

    /**
     * Creates a new short URL for the given request.
     * @param req request containing the long URL
     * @param httpReq HTTP request to extract client IP
     * @return ShortUrlResponseDto containing the generated short URL
     */
    @PostMapping
    public ResponseEntity<ShortUrlResponseDto> create(@Valid @RequestBody LongUrlRequestDto req,
                                                      HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        log.info("Received create request from ip={} for longUrl={}", ip, req.getLongUrl());

        // enforce rate limit
        if (!limiter.allow("create:" + ip, 60)) {
            log.warn("Rate limit exceeded for ip={}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        var entity = service.create(req);
        log.info("Created shortUrl={} for longUrl={}", entity.getShortUrl(), req.getLongUrl());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortUrlResponseDto(entity.getShortUrl()));
    }

    /**
     * Resolves a short URL code to its original long URL.
     * @param shortUrl short URL code
     * @return ResolveUrlResponseDto containing the long URL
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<ResolveUrlResponseDto> resolve(@PathVariable String shortUrl) {
        log.info("Received resolve request for shortUrl={}", shortUrl);

        return service.resolve(shortUrl)
                .map(longUrl -> {
                    log.info("Resolved shortUrl={} to longUrl={}", shortUrl, longUrl);
                    return ResponseEntity.ok(new ResolveUrlResponseDto(longUrl));
                })
                .orElseThrow(() -> {
                    log.warn("shortUrl={} not found", shortUrl);
                    return new UrlNotFoundException(shortUrl);
                });
    }
}
