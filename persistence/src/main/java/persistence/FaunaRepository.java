package persistence;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.errors.NotFoundException;
import com.faunadb.client.query.Expr;
import com.faunadb.client.query.Pagination;
import com.faunadb.client.types.Value;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import stock.bot.model.Entity;
import stock.bot.model.Page;
import stock.bot.model.PaginationOptions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.faunadb.client.query.Language.*;

@AllArgsConstructor
public abstract class FaunaRepository<T extends Entity> implements Repository<T>, IdentityFactory {
    @Autowired
    protected FaunaClient faunaClient;

    protected final Class<T> entityType;
    protected final String collectionName;
    protected final String collectionIndexName;

    @Override
    public CompletableFuture<String> nextId() {
        return faunaClient.query(
                NewId()
        ).thenApply(value -> value.to(String.class).get());
    }

    @Override
    public CompletableFuture<T> save(T entity) {
        return faunaClient.query(
                saveQuery(Value(entity.getId()), Value(entity))
        ).thenApply(this::toEntity);
    }

    @Override
    public CompletableFuture<Optional<T>> remove(String id) {
        CompletableFuture<T> result =
                faunaClient.query(
                        Select(
                                Value("data"),
                                Delete(Ref(Collection(collectionName), Value(id)))
                        )
                ).thenApply(this::toEntity);
        return toOptionalResult(result);
    }

    @Override
    public CompletableFuture<Optional<T>> find(String id) {
        CompletableFuture<T> result =
                faunaClient.query(
                        Select(
                                Value("data"),
                                Get(Ref(Collection(collectionName), Value(id)))
                        )
                ).thenApply(this::toEntity);
        return toOptionalResult(result);
    }

    @Override
    public CompletableFuture<Page<T>> findAll(PaginationOptions po) {
        Pagination paginationQuery = Paginate(Match(Index(Value(collectionName))));
        po.getSize().ifPresent(paginationQuery::size);
        po.getAfter().ifPresent(after -> paginationQuery.after(Ref(Collection(collectionName), Value(after))));
        po.getBefore().ifPresent(before -> paginationQuery.before(Ref(Collection(collectionName), Value(before))));
        return faunaClient.query(
                Map(paginationQuery, Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef")))))
        ).thenApply(this::toPage);
    }

    protected T toEntity(Value value) {
        return value.to(entityType).get();
    }

    protected CompletableFuture<Optional<T>> toOptionalResult(CompletableFuture<T> result) {
        return result.handle((v, t) -> {
            CompletableFuture<Optional<T>> r = new CompletableFuture<>();
            if (v != null) {
                r.complete(Optional.of(v));
            } else if (t != null && t.getCause() instanceof NotFoundException) {
                r.complete(Optional.empty());
            } else {
                r.completeExceptionally(t);
            }
            return r;
        }).thenCompose(Function.identity());
    }

    protected Page<T> toPage(Value value) {
        Optional<String> after = value.at("after").asCollectionOf(Value.RefV.class).map(c -> c.iterator().next().getId()).getOptional();
        Optional<String> before = value.at("before").asCollectionOf(Value.RefV.class).map(c -> c.iterator().next().getId()).getOptional();

        List<T> data = value.at("data").collect(entityType).stream().collect(Collectors.toList());

        Page<T> page = new Page(data, before, after);
        return page;
    }

    protected Expr saveQuery(Expr id, Expr data) {
        return Select(
                Value("data"),
                If(
                        Exists(Ref(Collection(collectionName), id)),
                        Replace(Ref(Collection(collectionName), id), Obj("data", data)),
                        Create(Ref(Collection(collectionName), id), Obj("data", data))
                )
        );
    }
}

