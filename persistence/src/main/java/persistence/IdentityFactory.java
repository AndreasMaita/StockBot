package persistence;

import java.util.concurrent.CompletableFuture;

public interface IdentityFactory {
    CompletableFuture<String> nextId();
}
