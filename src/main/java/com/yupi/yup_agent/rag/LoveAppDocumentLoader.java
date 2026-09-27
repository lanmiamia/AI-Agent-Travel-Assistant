package com.yupi.yup_agent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        // 加载多篇 Markdown 文档
        try {
            // 类路径classpath下document目录下,任意文件名.md
            // resources目录下的文件和java目录下的文件在编译时被统一编译到target/classes目录下
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename(); //获取文件名
                String status = filename.substring(filename.length() - 6, filename.length() - 4); //子串索引[倒六,倒四)
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder() // md格式文档读取器
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false) // 不包含代码块
                        .withIncludeBlockquote(false) // 不包含引用格式
                        .withAdditionalMetadata("filename", filename) // 添加额外元信息:文件名
                        .withAdditionalMetadata("status", status) //
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get());// 把List里面的所有元素添加到另一个List
            }

        } catch (IOException e) {
            log.error(" Markdown 文档加载失败 ", e);
        }
        return allDocuments;
    }
}
