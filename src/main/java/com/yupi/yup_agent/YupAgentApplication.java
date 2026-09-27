package com.yupi.yup_agent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class YupAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YupAgentApplication.class, args);
    }

}
