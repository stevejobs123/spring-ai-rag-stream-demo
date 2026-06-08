package saas.ai.service;

import saas.ai.entity.ChatEntity;

public interface ChatService {
     void doChat(ChatEntity chatEntity);
     void streamChat(ChatEntity chatEntity);

}
