package stock.bot.model;

public abstract class Entity {
    protected String id;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
