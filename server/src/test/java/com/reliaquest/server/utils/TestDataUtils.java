package com.reliaquest.server.utils;

import com.reliaquest.server.model.MockEmployee;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for creating test data for MockEmployee tests.
 * Provides consistent mock data across different test classes.
 */
public class TestDataUtils {

    /**
     * Creates a list of standard test employees with different salaries for testing.
     *
     * @return List of MockEmployee objects for testing
     */
    public static List<MockEmployee> createTestEmployees() {
        List<MockEmployee> employees = new ArrayList<>();

        employees.add(MockEmployee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@google.com")
                .build());

        employees.add(MockEmployee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(85000)
                .age(28)
                .title("Senior Developer")
                .email("jane.smith@google.com")
                .build());

        employees.add(MockEmployee.builder()
                .id(UUID.randomUUID())
                .name("Bob Wilson")
                .salary(95000)
                .age(35)
                .title("Tech Lead")
                .email("bob.wilson@google.com")
                .build());

        return employees;
    }

    /**
     * Creates a single test employee with default values.
     *
     * @return MockEmployee for testing
     */
    public static MockEmployee createSingleTestEmployee() {
        return MockEmployee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@google.com")
                .build();
    }

    /**
     * Creates a test employee with custom values.
     *
     * @param name Employee name
     * @param salary Employee salary
     * @param age Employee age
     * @param title Employee title
     * @param email Employee email
     * @return MockEmployee with specified values
     */
    public static MockEmployee createTestEmployee(
            String name, Integer salary, Integer age, String title, String email) {
        return MockEmployee.builder()
                .id(UUID.randomUUID())
                .name(name)
                .salary(salary)
                .age(age)
                .title(title)
                .email(email)
                .build();
    }

    /**
     * Creates a test employee with null salary for testing edge cases.
     *
     * @return MockEmployee with null salary
     */
    public static MockEmployee createEmployeeWithNullSalary() {
        return MockEmployee.builder()
                .id(UUID.randomUUID())
                .name("No Salary Employee")
                .salary(null)
                .age(30)
                .title("Volunteer")
                .email("volunteer@google.com")
                .build();
    }

    /**
     * Creates a test employee with null name for testing edge cases.
     *
     * @return MockEmployee with null name
     */
    public static MockEmployee createEmployeeWithNullName() {
        return MockEmployee.builder()
                .id(UUID.randomUUID())
                .name(null)
                .salary(100000)
                .age(30)
                .title("High Earner")
                .email("high@google.com")
                .build();
    }

    /**
     * Creates additional test employees for testing limits and large datasets.
     *
     * @param count Number of additional employees to create
     * @return List of MockEmployee objects
     */
    public static List<MockEmployee> createAdditionalTestEmployees(int count) {
        List<MockEmployee> employees = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            employees.add(MockEmployee.builder()
                    .id(UUID.randomUUID())
                    .name("Employee " + i)
                    .salary(50000 + i * 1000)
                    .age(25 + i)
                    .title("Developer " + i)
                    .email("employee" + i + "@google.com")
                    .build());
        }

        return employees;
    }
}
