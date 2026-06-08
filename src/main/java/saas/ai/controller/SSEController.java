package saas.ai.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import saas.ai.utils.SseServer;

@RestController
@RequestMapping("/sse")
public class SSEController {

    @GetMapping(path = "/connect", produces = {MediaType.TEXT_EVENT_STREAM_VALUE })
    public SseEmitter connect(@RequestParam String clientId) {

        return SseServer.connect(clientId);
    }
}
