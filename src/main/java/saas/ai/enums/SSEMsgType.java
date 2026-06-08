package saas.ai.enums;

public enum SSEMsgType {
    MESSAGE("message", "单次发送的普通信息"),
    ADD("add", "消息追加，适用于流式stream推送"),
    FINISH("finish", "消息发送完成"),
    CUSTOM_EVENT("custom_event", "自定义消息类型"),
    DONE("done", "消息发送完成");

    public final String type;
    public final String value;

    SSEMsgType(String type, String value) {

        this.type = type;
        this.value = value;
    }
}
