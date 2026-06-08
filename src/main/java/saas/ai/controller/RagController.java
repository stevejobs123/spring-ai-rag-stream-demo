package saas.ai.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import saas.ai.result.LeeResult;
import saas.ai.service.DocumentService;

@RestController
@RequestMapping("/rag")
@Slf4j
public class RagController {

    @Resource
    private DocumentService documentService;

    @PostMapping("/upload")
    public LeeResult upload(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        log.info("RAG upload request received, fileName: {}, size: {}", fileName, file.getSize());
        try {
            documentService.loadText(file.getResource(), fileName);
            log.info("RAG upload request completed, fileName: {}", fileName);
            return LeeResult.ok();
        } catch (RuntimeException e) {
            log.error("RAG upload request failed, fileName: {}", fileName, e);
            return LeeResult.fail("知识库上传失败：" + e.getMessage());
        }
    }
}
