package url.shortener.challenge.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ResolveUrlResponseDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import url.shortener.challenge.exception.UrlNotFoundException;
import url.shortener.challenge.service.UrlService;
import url.shortener.challenge.util.RateLimiter;


@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService service;
    private final RateLimiter limiter;

    public UrlController(UrlService service, RateLimiter limiter) {
        this.service = service;
        this.limiter = limiter;
    }

    /**
     * Creates a new short URL for the given request.
     * @param req
     * @param httpReq
     * @return ShortUrlResponseDto
     */
    @PostMapping
    public ResponseEntity<ShortUrlResponseDto> create( @Valid @RequestBody LongUrlRequestDto req,
            HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (!limiter.allow("create:" + ip, 60)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        var entity = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortUrlResponseDto(entity.getShortUrl()));
    }

    /**
     * Resolves a short URL code to its original long URL
     * @param shortUrl
     * @return ResolveUrlResponseDto
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<ResolveUrlResponseDto> resolve(@PathVariable String shortUrl) {
        return service.resolve(shortUrl)
                .map(longUrl -> ResponseEntity.ok(new ResolveUrlResponseDto(longUrl)))
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }
}
