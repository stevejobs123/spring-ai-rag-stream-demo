package saas.ai.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import saas.ai.entity.ChatEntity;
import saas.ai.enums.SSEMsgType;
import saas.ai.service.ChatService;
import saas.ai.service.DocumentService;
import saas.ai.utils.SseServer;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    @Resource
    private DocumentService documentService;

    private static final String RAG_PROMPT_TEMPLATE = """
            请根据下面提供的上下文知识库内容来回答用户的问题。
            规则：
            1. 回答时，要充分利用上下文信息，但不要在回答中直接提及“根据上下文”或“根据知识库”等词语。
            2. 如果上下文中没有足够的信息来回答问题，请明确告知：“根据现有的知识，我无法回答这个问题。”
            3. 你的回答应该是直接、清晰且相关的。

            【上下文】
            {context}

            【问题】
            {question}
            """;



    public ChatServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void doChat(ChatEntity chatEntity) {
        String userId = chatEntity.getCurrentUserName();
        String prompt = chatEntity.getMessage();

        Flux<String> stringFlux=chatClient
                .prompt(prompt)
                .stream()
                .content();

        stringFlux.doOnError(throwable -> {
            log.error("AI Stream error：" + throwable.getMessage());
            SseServer.sendMsg(userId, "AI service error", SSEMsgType.FINISH);
            SseServer.close(userId);

        }).subscribe(
                content -> SseServer.sendMsg(userId, content, SSEMsgType.ADD),
                error -> log.error("Error processing stream: " + error.getMessage()),
                () -> {
                    SseServer.sendMsg(userId, "done", SSEMsgType.FINISH);
                    SseServer.close(userId);
                }
        );

    }

    @Override
    public void streamChat(ChatEntity chatEntity) {
        String userId = chatEntity.getCurrentUserName();
        String question = chatEntity.getMessage();
        Prompt prompt;
        if (chatEntity.isUseKnowledgeBase()) {

            log.info("【用户: {}】正在使用知识库模式进行提问。", userId);
            // 1. 从向量数据库中搜索相关文档
            List<Document> relatedDocs = documentService.doSearch(question);

            // 2. 构建上下文
            String context = "没有找到相关的知识库信息。"; // 默认值
            if (relatedDocs != null && !relatedDocs.isEmpty()) {

                context = relatedDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n---\n"));
            }

            // 3. 创建RAG提示词
            String promptContent = RAG_PROMPT_TEMPLATE
                    .replace("{context}", context)
                    .replace("{question}", question);
            log.debug("RAG prompt content: {}", promptContent);
            prompt = new Prompt(promptContent);

        } else {

            log.info("【用户: {}】正在使用普通模式进行提问。", userId);
            // 普通模式，直接使用用户的问题作为提示词
            prompt = new Prompt(question);
        }
        // 统一的流式处理逻辑
        Flux<String> stream = chatClient.prompt(prompt).stream().content();

        // 订阅并处理流
        stream
                .doOnError(throwable -> {

                    log.error("【用户: {}】的AI流处理发生错误: {}", userId, throwable.getMessage(), throwable);
                    SseServer.sendMsg(userId, "抱歉，服务出现了一点问题，请稍后再试。", SSEMsgType.FINISH);
                    SseServer.close(userId);
                })
                .subscribe(
                        // onNext: 每当收到新的数据块时，通过SSE发送给前端
                        content -> SseServer.sendMsg(userId, content, SSEMsgType.ADD),

                        // onError: 最终的错误处理（虽然doOnError已经处理，但这是规范写法）
                        error -> log.error("【用户: {}】的流订阅最终失败: {}", userId, error.getMessage()),

                        // onComplete: 当流正常结束时，发送结束信号
                        () -> {

                            log.info("【用户: {}】的流已成功结束。", userId);
                            SseServer.sendMsg(userId, "done", SSEMsgType.FINISH);
                            SseServer.close(userId);
                        }
                );

    }
}
