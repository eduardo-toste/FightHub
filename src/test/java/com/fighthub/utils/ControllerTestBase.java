package com.fighthub.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.service.JwtService;
import com.fighthub.utils.errors.ErrorWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(TestSecurityConfig.class)
public abstract class ControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @MockBean protected JwtService jwtService;
    @MockBean protected TokenRepository tokenRepository;
    @MockBean protected UsuarioRepository usuarioRepository;
    @MockBean protected ErrorWriter errorWriter;

}
