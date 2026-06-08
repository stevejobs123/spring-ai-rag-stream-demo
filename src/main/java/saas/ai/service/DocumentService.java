package saas.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentService {
    void loadText(Resource resource, String fileName);

    List<Document> doSearch(String question);
}
