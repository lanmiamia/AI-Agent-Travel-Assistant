package com.yupi.yup_agent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

/*
* 恋爱大师向量数据库配置(初始化基于内存的向量数据库Bean)
* */

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    //该类的对象名即为方法名loveAppVectorStore
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载文档
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        //自主切分文档
        //List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        //自动提取并补充关键词元信息
        myKeywordEnricher.enrichDocuments(documentList);
        simpleVectorStore.add(documentList);
        return simpleVectorStore;
    }
}
