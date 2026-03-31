package tn.esprit.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.productservice.dto.request.CategoryRequest;
import tn.esprit.productservice.dto.response.CategoryResponse;
import tn.esprit.productservice.entities.Category;
import tn.esprit.productservice.mapper.ProductMapper;
import tn.esprit.productservice.services.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private CategoryController categoryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        // Préparer la requête
        CategoryRequest request = new CategoryRequest();
        request.setName("Nouvelle Catégorie");
        request.setDescription("Description test");
        request.setIsActive(true);

        // Préparer la réponse du service
        Category categoryEntity = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        CategoryResponse responseDto = new CategoryResponse();
        responseDto.setName(request.getName());
        responseDto.setDescription(request.getDescription());

        // Mocking
        when(categoryService.createCategory(any(Category.class), any())).thenReturn(categoryEntity);
        when(mapper.toCategoryResponse(categoryEntity)).thenReturn(responseDto);

        // Appel au endpoint
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nouvelle Catégorie"))
                .andExpect(jsonPath("$.data.description").value("Description test"));
    }
}