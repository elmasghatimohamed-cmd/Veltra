package ma.veltra.controllers;

import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.exceptions.GlobalExceptionHandler;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.services.ConducteurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConducteurControllerTest {

    private MockMvc mockMvc;
    private ConducteurService conducteurService;

    @BeforeEach
    void setUp() {
        conducteurService = mock(ConducteurService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConducteurController(conducteurService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn201WhenDriverCreated() throws Exception {
        ConducteurResponse response = new ConducteurResponse(
                1L, "El Masghati", "Mohamed",
                "mohamed@example.com", "0611111111", "PERMIS123"
        );

        when(conducteurService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/conducteurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "El Masghati",
                                  "prenom": "Mohamed",
                                  "email": "mohamed@example.com",
                                  "telephone": "0611111111",
                                  "numeroPermis": "PERMIS123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("mohamed@example.com"));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(conducteurService.create(any()))
                .thenThrow(new ResourceAlreadyExistsException(
                        "Un conducteur avec cet email existe déjà"
                ));

        mockMvc.perform(post("/api/conducteurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "El Masghati",
                                  "prenom": "Mohamed",
                                  "email": "mohamed@example.com",
                                  "telephone": "0611111111",
                                  "numeroPermis": "PERMIS123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/conducteurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "",
                                  "prenom": "Mohamed",
                                  "email": "invalid-email",
                                  "telephone": "",
                                  "numeroPermis": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conducteurService);
    }
}
