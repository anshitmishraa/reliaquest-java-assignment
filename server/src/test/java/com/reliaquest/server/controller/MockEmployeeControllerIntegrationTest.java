package com.reliaquest.server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.service.MockEmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MockEmployeeController.class)
@TestPropertySource(properties = "test.disable-interceptors=true")
class MockEmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MockEmployeeService mockEmployeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockEmployee testEmployee;
    private CreateMockEmployeeInput testInput;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testEmployee = MockEmployee.builder()
                .id(testId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@google.com")
                .build();

        testInput = new CreateMockEmployeeInput();
        testInput.setName("John Doe");
        testInput.setSalary(75000);
        testInput.setAge(30);
        testInput.setTitle("Software Engineer");
    }

    @Test
    void getAllEmployees_ShouldReturnEmployeesList() throws Exception {
        // Given
        List<MockEmployee> employees = Arrays.asList(testEmployee);
        when(mockEmployeeService.getMockEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$[0].employee_salary").value(75000))
                .andExpect(jsonPath("$[0].employee_age").value(30))
                .andExpect(jsonPath("$[0].employee_title").value("Software Engineer"));
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() throws Exception {
        // Given
        String searchString = "John";
        List<MockEmployee> matchingEmployees = Arrays.asList(testEmployee);
        when(mockEmployeeService.searchByName(searchString)).thenReturn(matchingEmployees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenExists() throws Exception {
        // Given
        when(mockEmployeeService.findById(testId)).thenReturn(Optional.of(testEmployee));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(mockEmployeeService.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", testId.toString())).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeById_ShouldReturnBadRequest_WhenInvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", "123e4567-e89b-12d3-a456-42661417400 ")).andExpect(status().isBadRequest());
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() throws Exception {
        // Given
        Integer highestSalary = 100000;
        when(mockEmployeeService.getHighestSalary()).thenReturn(Optional.of(highestSalary));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("100000"));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnNotFound_WhenNoEmployees() throws Exception {
        // Given
        when(mockEmployeeService.getHighestSalary()).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary")).andExpect(status().isNotFound());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopEarners() throws Exception {
        // Given
        List<String> topEarners = Arrays.asList("John Doe", "Jane Smith", "Bob Johnson");
        when(mockEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("John Doe"))
                .andExpect(jsonPath("$[1]").value("Jane Smith"))
                .andExpect(jsonPath("$[2]").value("Bob Johnson"));
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee_WhenValidInput() throws Exception {
        // Given
        when(mockEmployeeService.create(any(CreateMockEmployeeInput.class))).thenReturn(testEmployee);

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInput)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.employee_name").value("John Doe"));
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        // Given - Invalid input with missing required fields
        CreateMockEmployeeInput invalidInput = new CreateMockEmployeeInput();
        // name is null/blank - should fail validation

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenInvalidAge() throws Exception {
        // Given - Invalid age (below minimum)
        CreateMockEmployeeInput invalidInput = new CreateMockEmployeeInput();
        invalidInput.setName("John Doe");
        invalidInput.setSalary(75000);
        invalidInput.setAge(15); // Below minimum age of 16
        invalidInput.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenInvalidSalary() throws Exception {
        // Given - Invalid salary (negative)
        CreateMockEmployeeInput invalidInput = new CreateMockEmployeeInput();
        invalidInput.setName("John Doe");
        invalidInput.setSalary(-1000); // Negative salary
        invalidInput.setAge(30);
        invalidInput.setTitle("Software Engineer");

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeById_ShouldReturnOk_WhenEmployeeDeleted() throws Exception {
        // Given
        when(mockEmployeeService.deleteById(testId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully"));
    }

    @Test
    void deleteEmployeeById_ShouldReturnNotFound_WhenEmployeeNotFound() throws Exception {
        // Given
        when(mockEmployeeService.deleteById(testId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", testId.toString())).andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployeeById_ShouldReturnBadRequest_WhenInvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", "123e4567-e89b-12d3-a456-42661417400 "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid employee ID format"));
    }

    @Test
    void deleteEmployee_LegacyEndpoint_ShouldReturnSuccess() throws Exception {
        // Given
        DeleteMockEmployeeInput deleteInput = new DeleteMockEmployeeInput();
        deleteInput.setName("John Doe");
        when(mockEmployeeService.delete(any(DeleteMockEmployeeInput.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.status").value("Successfully processed request."));
    }

    @Test
    void getAllEmployees_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Given
        when(mockEmployeeService.getMockEmployees()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Given
        when(mockEmployeeService.searchByName(anyString())).thenThrow(new RuntimeException("Search error"));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/John")).andExpect(status().isInternalServerError());
    }
}
