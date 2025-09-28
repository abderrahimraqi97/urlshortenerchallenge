package url.shortener.challenge.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ShortUrlResponseDto {
    private String shortUrl;
}