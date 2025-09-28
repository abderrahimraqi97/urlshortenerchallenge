package url.shortener.challenge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import url.shortener.challenge.dto.LongUrlRequestDto;
import url.shortener.challenge.dto.ShortUrlResponseDto;
import url.shortener.challenge.service.UrlService;
import url.shortener.challenge.util.RateLimiter;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService service;

    @MockBean
    private RateLimiter limiter;

    @Test
    void create_ShouldReturn201WithShortUrl() throws Exception {
        // Mock rate limiter allows the request
        when(limiter.allow(any(), eq(60))).thenReturn(true);
        when(service.create(any(LongUrlRequestDto.class)))
                .thenReturn(new ShortUrlResponseDto("abc123"));

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com\"}")
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1"); // simulate client IP
                    return req;
                }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value("abc123"));
    }

    @Test
    void create_ShouldReturn429_WhenRateLimitExceeded() throws Exception {
        when(limiter.allow(any(), eq(60))).thenReturn(false);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longUrl\":\"https://example.com\"}")
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1");
                    return req;
                }))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void resolve_ShouldReturn200WithLongUrl() throws Exception {
        when(service.resolve("abc123"))
                .thenReturn(Optional.of("https://example.com"));

        mockMvc.perform(get("/api/v1/urls/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longUrl").value("https://example.com"));
    }

    @Test
    void resolve_ShouldReturn404_WhenUrlNotFound() throws Exception {
        when(service.resolve("notfound"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/urls/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn400_WhenLongUrlIsMissing() throws Exception {
        when(limiter.allow(any(), eq(60))).thenReturn(true);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}") // missing longUrl
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1");
                    return req;
                }))
                .andExpect(status().isBadRequest());
    }
}