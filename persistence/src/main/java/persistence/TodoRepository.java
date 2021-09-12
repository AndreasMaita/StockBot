package persistence;

import org.springframework.stereotype.Repository;
import stock.bot.data.TodoEntity;

@Repository
public class TodoRepository extends FaunaRepository<TodoEntity> {

    public TodoRepository() {
        super(TodoEntity.class, "todos", "all_todos");
    }
}
