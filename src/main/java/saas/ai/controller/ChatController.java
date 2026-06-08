package saas.ai.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import saas.ai.entity.ChatEntity;
import saas.ai.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService  chatService;

    private ChatClient chatClient;
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();

    }

    @GetMapping("/test")
    public String test(@RequestParam String mes) {
        return chatClient.prompt()
                .user(mes)
                .call()
                .content();
    }


    @PostMapping("/ai")
    public void chat(@RequestBody ChatEntity chatEntity) {
        chatService.doChat(chatEntity);
    }

    /**
     * 统一的聊天接口
     * @param chatEntity 包含消息和是否使用知识库的标志
     */
    @PostMapping("/send")
    public void chat2(@RequestBody ChatEntity chatEntity) {

        // 直接将包含所有信息的实体传递给服务层
        chatService.streamChat(chatEntity);
    }

}
