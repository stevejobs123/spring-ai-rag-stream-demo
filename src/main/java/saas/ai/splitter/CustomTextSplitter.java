package saas.ai.splitter;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.List;

public class CustomTextSplitter extends TextSplitter {
    @Override
    protected List<String> splitText(String text) {
        return List.of(split(text));
    }

    public String[] split(String text) {

        // 为简化示例，我们按空行进行分割
        return text.split("\\R\\s*\\R");
    }
}
