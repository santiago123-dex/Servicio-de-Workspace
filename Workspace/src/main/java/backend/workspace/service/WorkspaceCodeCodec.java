package backend.workspace.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class WorkspaceCodeCodec {

    public String encode(String plainCode) {
        return Base64.getEncoder().encodeToString(plainCode.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(String encodedCode) {
        byte[] bytes = Base64.getDecoder().decode(encodedCode);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
