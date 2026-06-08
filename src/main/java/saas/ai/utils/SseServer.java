package saas.ai.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import saas.ai.enums.SSEMsgType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SseServer {
    private static final Map<String, SseEmitter> sseClients = new ConcurrentHashMap<>();
    public static SseEmitter connect(String clientId) {
        SseEmitter sseEmitter = new SseEmitter();
        sseEmitter.onCompletion(() -> sseClients.remove(clientId));
        sseEmitter.onError(e -> sseClients.remove(clientId));
        sseClients.put(clientId, sseEmitter);
        log.info("SSE,Connect,clientId{}:",clientId);
        return sseEmitter;
    }


    public static void sendMsg(String clientId, String msg, SSEMsgType msgType) {
        if (CollectionUtils.isEmpty(sseClients)) {
            return;
        }
        if (sseClients.containsKey(clientId)) {
            SseEmitter sseEmitter = sseClients.get(clientId);
            sendEmitterMsg(sseEmitter,clientId,msg,msgType);
        }
    }
    public static void sendMsgToAll(String msg) {
        if (CollectionUtils.isEmpty(sseClients)) {
            return;
        }
        sseClients.forEach((clientId, sseEmitter) -> {
            sendEmitterMsg(sseEmitter,clientId,msg,SSEMsgType.MESSAGE);

        });
    }


    private static void sendEmitterMsg(SseEmitter sseEmitter,String clientId,String msg, SSEMsgType msgType) {
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(clientId)
                .data(msg)
                .name(msgType.type);
        try{
            sseEmitter.send(event);
        }catch (Exception e){
            log.error("SSE send message error, clientId: {}, error: {}", clientId, e.getMessage());
            close(clientId);
        }

    }
    public static void close(String userId) {

        SseEmitter emitter = sseClients.get(userId);
        if (emitter != null) {
            emitter.complete(); // 这会触发 onCompletion 回调,回调中已经包含了 remove 操作
        }
    }

}
