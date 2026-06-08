package saas.ai.result;

public class LeeResult<T> {
    private final int code;
    private final String message;
    private final T data;

    private LeeResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static LeeResult<Void> ok() {
        return new LeeResult<>(200, "success", null);
    }

    public static <T> LeeResult<T> ok(T data) {
        return new LeeResult<>(200, "success", data);
    }

    public static LeeResult<Void> fail(String message) {
        return new LeeResult<>(500, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
