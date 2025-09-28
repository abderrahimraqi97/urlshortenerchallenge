package url.shortener.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import url.shortener.challenge.config.AppProperties;
import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import url.shortener.challenge.exception.AliasAlreadyExistsException;
import url.shortener.challenge.entity.Url;
import url.shortener.challenge.repository.UrlRepository;
import url.shortener.challenge.service.CodeGenerator;
import url.shortener.challenge.service.impl.UrlServiceImpl;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class UrlServiceImplTest {

    @Mock
    private UrlRepository repo;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private AppProperties props;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private UrlServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redis.opsForValue()).thenReturn(valueOps);

        // Defaults to avoid NPE
        when(props.getCodeLength()).thenReturn(6);
        when(props.getDefaultTtlSeconds()).thenReturn(60L);
    }

    @Test
    void create_ShouldGenerateShortUrl() {
        LongUrlRequestDto req = new LongUrlRequestDto("https://example.com");

        when(repo.existsByShortUrl(any())).thenReturn(false);
        when(codeGenerator.generate(anyInt())).thenReturn("abc123");

        ShortUrlResponseDto response = service.create(req);

        assertThat(response.getShortUrl()).isEqualTo("abc123");
        verify(repo).save(any(Url.class));
    }

    @Test
    void create_ShouldThrowIfAliasAlreadyExists() {
        LongUrlRequestDto req = new LongUrlRequestDto("https://example.com");

        when(repo.existsByShortUrl(any())).thenReturn(true);
        when(codeGenerator.generate(anyInt())).thenReturn("abc123");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(AliasAlreadyExistsException.class);
    }

    @Test
    void resolve_ShouldReturnFromCache() {
        String shortUrl = "cache123";
        when(valueOps.get("url:" + shortUrl)).thenReturn("https://cached.com");

        Optional<String> result = service.resolve(shortUrl);

        assertThat(result).contains("https://cached.com");
        verify(valueOps).get("url:" + shortUrl);
    }

    @Test
    void resolve_ShouldReturnFromDatabase_WhenNotInCache() {
        String shortUrl = "db123";
        Url entity = new Url();
        entity.setShortUrl(shortUrl);
        entity.setLongUrl("https://db.com");
        entity.setExpiresAt(Instant.now().plusSeconds(60));

        when(valueOps.get("url:" + shortUrl)).thenReturn(null);
        when(repo.findByShortUrl(shortUrl)).thenReturn(Optional.of(entity));

        Optional<String> result = service.resolve(shortUrl);

        assertThat(result).contains("https://db.com");
        verify(repo, atLeastOnce()).findByShortUrl(shortUrl);
        verify(valueOps).set(eq("url:" + shortUrl), eq("https://db.com"), any());
    }

    @Test
    void resolve_ShouldReturnEmpty_WhenUrlExpired() {
        String shortUrl = "expired123";
        Url entity = new Url();
        entity.setShortUrl(shortUrl);
        entity.setLongUrl("https://expired.com");
        entity.setExpiresAt(Instant.now().minusSeconds(60)); // already expired

        when(valueOps.get("url:" + shortUrl)).thenReturn(null);
        when(repo.findByShortUrl(shortUrl)).thenReturn(Optional.of(entity));

        Optional<String> result = service.resolve(shortUrl);

        assertThat(result).isEmpty();
    }
}
