package persistence;

import stock.bot.model.Entity;
import stock.bot.model.Page;
import stock.bot.model.PaginationOptions;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Repository<T extends Entity> {
    CompletableFuture<T> save(T entity);

    CompletableFuture<Optional<T>> find(String id);

    CompletableFuture<Page<T>> findAll(PaginationOptions po);

    CompletableFuture<Optional<T>> remove(String id);
}
