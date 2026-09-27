package com.yupi.yup_agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
@Configuration
public class PgVectorVectorStoreConfig {

    // 读进本地.md文档
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    // 方法中的传参也会按照先类型后名称的方式自动注入
    // dashscopeEmbeddingModel阿里灵积大模型默认一次性只能插入25条,也就是List长度需要小于25
    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel){
        // 创建表(if not exists)
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 不要盲目设置
                .distanceType(COSINE_DISTANCE)       // 相似度计算设置:余弦相似度
                .indexType(HNSW)                     // 索引类型
                .initializeSchema(true)              // 自动初始化建表
                .schemaName("public")                // 表名称
                .vectorTableName("vector_store")     // 向量表名称
                .maxDocumentBatchSize(10000)         // 单次最高插入10000条
                .build();

        // 读入.md文档得到 List表
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 执行SQL语句添加到向量数据库表中
        vectorStore.add(documents);
        return vectorStore;
    }


}
