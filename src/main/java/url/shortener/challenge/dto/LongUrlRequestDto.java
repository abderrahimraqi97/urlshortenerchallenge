package url.shortener.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LongUrlRequestDto {
    @NotBlank(message = "Long URL is required")
    @Pattern(regexp = "^(https?://).+", message = "Must start with http:// or https://")
    private String longUrl;
}
