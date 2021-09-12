package stock.bot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {
    @GetMapping("/test")
    public String getTest() {
        return "Test";
    }
}
