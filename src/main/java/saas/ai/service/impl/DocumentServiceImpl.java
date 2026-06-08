package saas.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import saas.ai.service.DocumentService;
import saas.ai.splitter.CustomTextSplitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final VectorStore vectorStore;

    @Override
    public void loadText(Resource resource, String fileName) {
        long startTime = System.currentTimeMillis();
        log.info("RAG document load started, fileName: {}", fileName);
        // 加载读取文档
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("fileName", fileName);
        List<Document> documents = textReader.get();
        log.info("RAG document read completed, fileName: {}, documentCount: {}", fileName, documents.size());
        // 切割文档
        CustomTextSplitter customTextSplitter = new CustomTextSplitter();
        List<Document> list = customTextSplitter.apply(documents);
        log.info("RAG document split completed, fileName: {}, chunkCount: {}", fileName, list.size());
        // 向量存储
        log.info("RAG vector store add started, fileName: {}, chunkCount: {}", fileName, list.size());
        try {
            vectorStore.add(list);
        } catch (RuntimeException e) {
            log.error("RAG vector store add failed, fileName: {}, chunkCount: {}", fileName, list.size(), e);
            throw e;
        }
        log.info(
                "RAG document load completed, fileName: {}, chunkCount: {}, costMs: {}",
                fileName,
                list.size(),
                System.currentTimeMillis() - startTime
        );
    }

    @Override
    public List<Document> doSearch(String question) {
        log.info("RAG document search started, question: {}", question);
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(0.6)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        log.info("RAG document search completed, question: {}, resultCount: {}", question, documents.size());
        return documents;
    }
}
