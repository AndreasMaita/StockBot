package stock.bot;

import com.faunadb.client.FaunaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class StockBotApplication {
    @Value("${fauna-db.secret}")
    private String serverKey;

    public static void main(String[] args) {
        SpringApplication.run(StockBotApplication.class, args);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FaunaClient faunaConfiguration() {
        return FaunaClient.builder()
                .withSecret(serverKey).build();
    }
}
