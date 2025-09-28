package url.shortener.challenge.service;

import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import java.util.Optional;

public interface UrlService {
    ShortUrlResponseDto create(LongUrlRequestDto request);
    Optional<String> resolve(String shortUrl);
}
