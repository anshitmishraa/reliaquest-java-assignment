package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.CreateMockEmployeeInput;
import com.reliaquest.api.model.DeleteMockEmployeeInput;
import com.reliaquest.api.model.MockEmployee;
import com.reliaquest.api.utils.TestDataUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.datafaker.Faker;
import net.datafaker.providers.base.Twitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockEmployeeServiceTest {

    @Mock
    private Faker faker;

    @Mock
    private Twitter twitter;

    private MockEmployeeService mockEmployeeService;
    private List<MockEmployee> testEmployees;

    @BeforeEach
    void setUp() {
        testEmployees = new ArrayList<>(TestDataUtils.createTestEmployees());
        mockEmployeeService = new MockEmployeeService(faker, testEmployees);
    }

    @Test
    void findById_ShouldReturnEmployee_WhenEmployeeExists() {
        // Given
        MockEmployee targetEmployee = testEmployees.get(0);
        UUID targetId = targetEmployee.getId();

        // When
        Optional<MockEmployee> result = mockEmployeeService.findById(targetId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(targetEmployee);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenEmployeeDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<MockEmployee> result = mockEmployeeService.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void create_ShouldAddNewEmployee_WhenValidInput() {
        // Given
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("Martin Fowler");
        input.setSalary(70000);
        input.setAge(25);
        input.setTitle("Junior Developer");

        when(faker.twitter()).thenReturn(twitter);
        when(twitter.userName()).thenReturn("Martin Fowler");

        int initialSize = testEmployees.size();

        // When
        MockEmployee result = mockEmployeeService.create(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Martin Fowler");
        assertThat(result.getSalary()).isEqualTo(70000);
        assertThat(result.getAge()).isEqualTo(25);
        assertThat(result.getTitle()).isEqualTo("Junior Developer");
        assertThat(result.getId()).isNotNull();
        assertThat(testEmployees).hasSize(initialSize + 1);
        assertThat(testEmployees).contains(result);
    }

    @Test
    void delete_ShouldRemoveEmployee_WhenEmployeeExists() {
        // Given
        DeleteMockEmployeeInput input = new DeleteMockEmployeeInput();
        input.setName("John Doe");
        int initialSize = testEmployees.size();

        // When
        boolean result = mockEmployeeService.delete(input);

        // Then
        assertThat(result).isTrue();
        assertThat(testEmployees).hasSize(initialSize - 1);
        assertThat(testEmployees.stream().noneMatch(emp -> "John Doe".equals(emp.getName())))
                .isTrue();
    }

    @Test
    void delete_ShouldReturnFalse_WhenEmployeeDoesNotExist() {
        // Given
        DeleteMockEmployeeInput input = new DeleteMockEmployeeInput();
        input.setName("Kate Moss");
        int initialSize = testEmployees.size();

        // When
        boolean result = mockEmployeeService.delete(input);

        // Then
        assertThat(result).isFalse();
        assertThat(testEmployees).hasSize(initialSize);
    }

    @Test
    void deleteById_ShouldRemoveEmployee_WhenEmployeeExists() {
        // Given
        MockEmployee targetEmployee = testEmployees.get(0);
        UUID targetId = targetEmployee.getId();
        int initialSize = testEmployees.size();

        // When
        boolean result = mockEmployeeService.deleteById(targetId);

        // Then
        assertThat(result).isTrue();
        assertThat(testEmployees).hasSize(initialSize - 1);
        assertThat(testEmployees).doesNotContain(targetEmployee);
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenEmployeeDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        int initialSize = testEmployees.size();

        // When
        boolean result = mockEmployeeService.deleteById(nonExistentId);

        // Then
        assertThat(result).isFalse();
        assertThat(testEmployees).hasSize(initialSize);
    }

    @Test
    void searchByName_ShouldReturnMatchingEmployees_WhenSearchStringMatches() {
        // Given
        String searchString = "John";

        // When
        List<MockEmployee> result = mockEmployeeService.searchByName(searchString);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void searchByName_ShouldReturnMultipleEmployees_WhenMultipleMatches() {
        // Given
        // Add another employee with "John" in the name
        testEmployees.add(
                TestDataUtils.createTestEmployee("Johnny Cash", 60000, 40, "Musician", "johnny.cash@google.com"));

        String searchString = "John";

        // When
        List<MockEmployee> result = mockEmployeeService.searchByName(searchString);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(emp -> emp.getName().toLowerCase().contains("john")))
                .isTrue();
    }

    @Test
    void searchByName_ShouldReturnEmptyList_WhenNoMatches() {
        // Given
        String searchString = "Patrick Star";

        // When
        List<MockEmployee> result = mockEmployeeService.searchByName(searchString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchByName_ShouldReturnEmptyList_WhenSearchStringIsEmpty() {
        // Given
        String searchString = "";

        // When
        List<MockEmployee> result = mockEmployeeService.searchByName(searchString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchByName_ShouldBeCaseInsensitive() {
        // Given
        String searchString = "JOHN";

        // When
        List<MockEmployee> result = mockEmployeeService.searchByName(searchString);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void getHighestSalary_ShouldReturnHighestSalary_WhenEmployeesExist() {
        // When
        Optional<Integer> result = mockEmployeeService.getHighestSalary();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(95000); // Bob Wilson has the highest salary
    }

    @Test
    void getHighestSalary_ShouldReturnEmpty_WhenNoEmployeesExist() {
        // Given
        testEmployees.clear();

        // When
        Optional<Integer> result = mockEmployeeService.getHighestSalary();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHighestSalary_ShouldIgnoreEmployeesWithNullSalary() {
        // Given
        testEmployees.add(TestDataUtils.createEmployeeWithNullSalary());

        // When
        Optional<Integer> result = mockEmployeeService.getHighestSalary();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(95000); // Still Bob Wilson's salary
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnNamesInDescendingOrder() {
        // When
        List<String> result = mockEmployeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Bob Wilson", "Jane Smith", "John Doe");
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnMaxTenEmployees() {
        // Given - Add more employees to test the limit
        testEmployees.addAll(TestDataUtils.createAdditionalTestEmployees(15));

        // When
        List<String> result = mockEmployeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(result).hasSize(10);
        // The highest should be "Bob Wilson" (95000), then "Employee 14" (64000), etc.
        assertThat(result.get(0)).isEqualTo("Bob Wilson");
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnEmptyList_WhenNoEmployees() {
        // Given
        testEmployees.clear();

        // When
        List<String> result = mockEmployeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldIgnoreEmployeesWithNullSalaryOrName() {
        // Given
        testEmployees.add(TestDataUtils.createEmployeeWithNullName());
        testEmployees.add(TestDataUtils.createEmployeeWithNullSalary());

        // When
        List<String> result = mockEmployeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertThat(result).hasSize(3); // Only the original 3 employees
        assertThat(result).containsExactly("Bob Wilson", "Jane Smith", "John Doe");
    }

    @Test
    void getMockEmployees_ShouldReturnAllEmployees() {
        // When
        List<MockEmployee> result = mockEmployeeService.getMockEmployees();

        // Then
        assertThat(result).isEqualTo(testEmployees);
        assertThat(result).hasSize(3);
    }
}
