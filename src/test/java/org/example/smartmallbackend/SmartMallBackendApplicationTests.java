package org.example.smartmallbackend;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartMallBackendApplicationTests {
    @Autowired
    private ChatLanguageModel chatLanguageModel;
    @Test
    void contextLoads() {
        String response = chatLanguageModel.generate("Hello, LangChain4j!");
        System.out.println("Response from ChatLanguageModel: " + response);
    }
}
