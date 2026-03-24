package link.botwmcs.fizzy.client.formatting.placeholder;

import java.util.Optional;

public interface PlaceholderResolver {
    String id();

    Optional<PlaceholderToken> resolve(String payload, PlaceholderContext context);
}
