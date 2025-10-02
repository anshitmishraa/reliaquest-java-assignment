package com.reliaquest.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.service.MockEmployeeService;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MockEmployeeControllerTest {

    @Mock
    private MockEmployeeService mockEmployeeService;

    @Mock
    private Validator validator;

    @InjectMocks
    private MockEmployeeController mockEmployeeController;

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
    void getAllEmployees_ShouldReturnAllEmployees_WhenEmployeesExist() {
        // Given
        List<MockEmployee> employees = Arrays.asList(testEmployee);
        when(mockEmployeeService.getMockEmployees()).thenReturn(employees);

        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getAllEmployees();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).isEqualTo(testEmployee);
        verify(mockEmployeeService, times(1)).getMockEmployees();
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        // Given
        when(mockEmployeeService.getMockEmployees()).thenReturn(List.of());

        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getAllEmployees();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(mockEmployeeService, times(1)).getMockEmployees();
    }

    @Test
    void getAllEmployees_ShouldReturnInternalServerError_WhenServiceThrowsException() {
        // Given
        when(mockEmployeeService.getMockEmployees()).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getAllEmployees();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees_WhenSearchStringMatches() {
        // Given
        String searchString = "John";
        List<MockEmployee> matchingEmployees = Arrays.asList(testEmployee);
        when(mockEmployeeService.searchByName(searchString)).thenReturn(matchingEmployees);

        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getEmployeesByNameSearch(searchString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).isEqualTo(testEmployee);
        verify(mockEmployeeService, times(1)).searchByName(searchString);
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnEmptyList_WhenNoMatches() {
        // Given
        String searchString = "NonExistent";
        when(mockEmployeeService.searchByName(searchString)).thenReturn(List.of());

        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getEmployeesByNameSearch(searchString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(mockEmployeeService, times(1)).searchByName(searchString);
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnBadRequest_WhenSearchStringIsEmpty() {
        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getEmployeesByNameSearch("");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnBadRequest_WhenSearchStringIsNull() {
        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getEmployeesByNameSearch(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnBadRequest_WhenSearchStringIsBlank() {
        // When
        ResponseEntity<List<MockEmployee>> response = mockEmployeeController.getEmployeesByNameSearch("   ");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() {
        // Given
        String idString = testId.toString();
        when(mockEmployeeService.findById(testId)).thenReturn(Optional.of(testEmployee));

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.getEmployeeById(idString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testEmployee);
        verify(mockEmployeeService, times(1)).findById(testId);
    }

    @Test
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist() {
        // Given
        String idString = testId.toString();
        when(mockEmployeeService.findById(testId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.getEmployeeById(idString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(mockEmployeeService, times(1)).findById(testId);
    }

    @Test
    void getEmployeeById_ShouldReturnBadRequest_WhenInvalidUUID() {
        // Given
        String invalidId = "invalid-uuid";

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.getEmployeeById(invalidId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary_WhenEmployeesExist() {
        // Given
        Integer highestSalary = 100000;
        when(mockEmployeeService.getHighestSalary()).thenReturn(Optional.of(highestSalary));

        // When
        ResponseEntity<Integer> response = mockEmployeeController.getHighestSalaryOfEmployees();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(highestSalary);
        verify(mockEmployeeService, times(1)).getHighestSalary();
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnNotFound_WhenNoEmployeesWithSalary() {
        // Given
        when(mockEmployeeService.getHighestSalary()).thenReturn(Optional.empty());

        // When
        ResponseEntity<Integer> response = mockEmployeeController.getHighestSalaryOfEmployees();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(mockEmployeeService, times(1)).getHighestSalary();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopEarners_WhenEmployeesExist() {
        // Given
        List<String> topEarners = Arrays.asList("John Doe", "Jane Smith", "Bob Johnson");
        when(mockEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        // When
        ResponseEntity<List<String>> response = mockEmployeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()).containsExactly("John Doe", "Jane Smith", "Bob Johnson");
        verify(mockEmployeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnEmptyList_WhenNoEmployees() {
        // Given
        when(mockEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(List.of());

        // When
        ResponseEntity<List<String>> response = mockEmployeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(mockEmployeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee_WhenValidInput() {
        // Given
        when(validator.validate(testInput)).thenReturn(Collections.emptySet());
        when(mockEmployeeService.create(testInput)).thenReturn(testEmployee);

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.createEmployee(testInput);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(testEmployee);
        verify(mockEmployeeService, times(1)).create(testInput);
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenValidationFails() {
        // Given
        @SuppressWarnings("unchecked")
        jakarta.validation.ConstraintViolation<CreateMockEmployeeInput> violation =
                mock(jakarta.validation.ConstraintViolation.class);
        Set<jakarta.validation.ConstraintViolation<CreateMockEmployeeInput>> violations = Set.of(violation);
        when(validator.validate(testInput)).thenReturn(violations);

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.createEmployee(testInput);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void createEmployee_ShouldReturnBadRequest_WhenInputIsNull() {
        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.createEmployee(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void createEmployee_ShouldReturnInternalServerError_WhenServiceThrowsException() {
        // Given
        when(validator.validate(testInput)).thenReturn(Collections.emptySet());
        when(mockEmployeeService.create(any(CreateMockEmployeeInput.class)))
                .thenThrow(new RuntimeException("Creation failed"));

        // When
        ResponseEntity<MockEmployee> response = mockEmployeeController.createEmployee(testInput);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteEmployeeById_ShouldReturnOk_WhenEmployeeDeleted() {
        // Given
        String idString = testId.toString();
        when(mockEmployeeService.deleteById(testId)).thenReturn(true);

        // When
        ResponseEntity<String> response = mockEmployeeController.deleteEmployeeById(idString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Employee deleted successfully");
        verify(mockEmployeeService, times(1)).deleteById(testId);
    }

    @Test
    void deleteEmployeeById_ShouldReturnNotFound_WhenEmployeeNotFound() {
        // Given
        String idString = testId.toString();
        when(mockEmployeeService.deleteById(testId)).thenReturn(false);

        // When
        ResponseEntity<String> response = mockEmployeeController.deleteEmployeeById(idString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(mockEmployeeService, times(1)).deleteById(testId);
    }

    @Test
    void deleteEmployeeById_ShouldReturnBadRequest_WhenInvalidUUID() {
        // Given
        String invalidId = "invalid-uuid";

        // When
        ResponseEntity<String> response = mockEmployeeController.deleteEmployeeById(invalidId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid employee ID format");
    }

    @Test
    void deleteEmployeeById_ShouldReturnInternalServerError_WhenServiceThrowsException() {
        // Given
        String idString = testId.toString();
        when(mockEmployeeService.deleteById(testId)).thenThrow(new RuntimeException("Deletion failed"));

        // When
        ResponseEntity<String> response = mockEmployeeController.deleteEmployeeById(idString);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");
    }
}
