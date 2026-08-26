package com.codeforge.module.submission.controller;

import com.codeforge.common.enums.ProgrammingLanguage;
import com.codeforge.common.enums.SubmissionStatus;
import com.codeforge.common.enums.SubmissionVerdict;
import com.codeforge.common.exception.GlobalExceptionHandler;
import com.codeforge.module.submission.dto.request.CreateSubmissionRequest;
import com.codeforge.module.submission.dto.response.SubmissionResponse;
import com.codeforge.module.submission.service.SubmissionService;
import com.codeforge.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SubmissionController submissionController;

    private UserPrincipal testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserPrincipal(
                1L, "john_doe", "john@example.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(submissionController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/submissions should return 202 Accepted with PENDING status")
    void shouldCreateSubmission() throws Exception {
        CreateSubmissionRequest request = CreateSubmissionRequest.builder()
                .problemId(1L)
                .language(ProgrammingLanguage.JAVA)
                .sourceCode("public class Solution {}")
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .id(100L)
                .problemId(1L)
                .problemSlug("two-sum")
                .problemTitle("Two Sum")
                .userId(1L)
                .username("john_doe")
                .language(ProgrammingLanguage.JAVA)
                .status(SubmissionStatus.PENDING)
                .build();

        when(submissionService.createSubmission(any(UserPrincipal.class), any(CreateSubmissionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/submissions/{id} should return 200 OK with submission details")
    void shouldGetSubmissionById() throws Exception {
        SubmissionResponse response = SubmissionResponse.builder()
                .id(100L)
                .problemId(1L)
                .problemSlug("two-sum")
                .problemTitle("Two Sum")
                .userId(1L)
                .username("john_doe")
                .language(ProgrammingLanguage.CPP)
                .status(SubmissionStatus.COMPLETED)
                .verdict(SubmissionVerdict.ACCEPTED)
                .executionTimeMs(32)
                .memoryUsedKb(8400)
                .build();

        when(submissionService.getSubmissionById(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/submissions/100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.verdict").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.executionTimeMs").value(32));
    }
}
