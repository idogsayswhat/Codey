package io.horrorshow.codey.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

class WandboxApiTest {

    @Mock
    RestTemplate restTemplate;
    WandboxApi wandboxApi;
    WandboxConfiguration config;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        config = new WandboxConfiguration();
        config.setUrl("WandboxURL");
        config.getCompiler().put("java", "java-compiler");
        wandboxApi = new WandboxApi(restTemplate, config);
    }

    @Test
    void compile_java_with_default_compiler_and_apply_language_specific_fixes() {
        var codeBlock = """
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }""";
        var expectedCode = """
                class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }""";
        var expectedGenRequest = new WandboxRequest(expectedCode, config.getCompiler().get("java"),
                null, null);
        var expResponse = new WandboxResponse();
        when(restTemplate
                .postForObject(
                        eq(config.getUrl()), eq(expectedGenRequest), eq(WandboxResponse.class)))
                .thenReturn(expResponse);

        var result = wandboxApi.compile(codeBlock, "java", null, null);

        assertThat(result).isEqualTo(expResponse);
    }
}