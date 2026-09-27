package com.yupi.yup_agent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
@SpringBootTest
class PgVectorVectorStoreConfigTest {

    //执行创建表的SQL语句
    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        // 创建要添加的内容
        List<Document> documents = List.of(
                new Document("鱼皮的编程导航有什么用?学编程啊,做项目啊", Map.of("meta1", "meta1")),
                new Document("程序员鱼皮的原创项目教程 codefather.cn",Map.of("meta3", "meta3")),
                new Document("鱼皮这小伙子比较帅气", Map.of("meta2", "meta2")));
        // 执行添加文档的SQL语句
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学编程啊").topK(3).build());
        Assertions.assertNotNull(results);


    }
}

