package url.shortener.challenge.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("urls")
@CompoundIndexes({
        @CompoundIndex(name="short_url_unique_idx", def="{ 'shortUrl': 1 }", unique = true)
})
public class Url {

    @Id
    private String id;

    @Indexed(unique = true)
    private String shortUrl;

    @Indexed
    private String longUrl;

    private Instant createdAt = Instant.now();

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    private long hitCount;
}

