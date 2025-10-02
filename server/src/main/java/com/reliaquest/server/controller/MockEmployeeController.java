package com.reliaquest.server.controller;

import com.reliaquest.api.controller.IEmployeeController;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.Response;
import com.reliaquest.server.service.MockEmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Validated
public class MockEmployeeController implements IEmployeeController<MockEmployee, CreateMockEmployeeInput> {

    private final MockEmployeeService mockEmployeeService;
    private final Validator validator;

    @Override
    @GetMapping()
    public ResponseEntity<List<MockEmployee>> getAllEmployees() {
        log.info("Fetching all employees");
        try {
            final var employees = mockEmployeeService.getMockEmployees();
            log.info("Successfully retrieved {} employees", employees.size());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error fetching all employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<MockEmployee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.info("Searching employees by name: {}", searchString);

        // Manual validation since we can't use @NotBlank on interface implementation
        if (searchString == null || searchString.trim().isEmpty()) {
            log.warn("Received null or empty search string");
            return ResponseEntity.badRequest().build();
        }

        try {
            final var employees = mockEmployeeService.searchByName(searchString);
            log.info("Found {} employees matching search: {}", employees.size(), searchString);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MockEmployee> getEmployeeById(@PathVariable String id) {
        log.info("Fetching employee by ID: {}", id);
        try {
            final var uuid = UUID.fromString(id);
            return mockEmployeeService
                    .findById(uuid)
                    .map(employee -> {
                        log.info("Successfully found employee with ID: {}", id);
                        return ResponseEntity.ok(employee);
                    })
                    .orElseGet(() -> {
                        log.warn("Employee not found with ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching employee by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Fetching highest salary of all employees");
        try {
            return mockEmployeeService
                    .getHighestSalary()
                    .map(salary -> {
                        log.info("Highest salary found: {}", salary);
                        return ResponseEntity.ok(salary);
                    })
                    .orElseGet(() -> {
                        log.warn("No employees with valid salary found");
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching highest salary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names");
        try {
            final var topEarners = mockEmployeeService.getTopTenHighestEarningEmployeeNames();
            log.info("Successfully retrieved {} top earning employee names", topEarners.size());
            return ResponseEntity.ok(topEarners);
        } catch (Exception e) {
            log.error("Error fetching top 10 highest earning employee names", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PostMapping()
    public ResponseEntity<MockEmployee> createEmployee(@RequestBody CreateMockEmployeeInput employeeInput) {
        log.info("Creating new employee: {}", employeeInput != null ? employeeInput.getName() : "null");

        // Manual validation since we can't use @Valid on interface implementation
        if (employeeInput == null) {
            log.warn("Received null employee input");
            return ResponseEntity.badRequest().build();
        }

        Set<ConstraintViolation<CreateMockEmployeeInput>> violations = validator.validate(employeeInput);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for employee creation: {}", violations);
            return ResponseEntity.badRequest().build();
        }

        try {
            final var createdEmployee = mockEmployeeService.create(employeeInput);
            log.info("Successfully created employee with ID: {}", createdEmployee.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            log.error("Error creating employee: {}", employeeInput.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Deleting employee by ID: {}", id);
        try {
            final var uuid = UUID.fromString(id);
            final boolean deleted = mockEmployeeService.deleteById(uuid);
            if (deleted) {
                log.info("Successfully deleted employee with ID: {}", id);
                return ResponseEntity.ok("Employee deleted successfully");
            } else {
                log.warn("Employee not found for deletion with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid employee ID format");
        } catch (Exception e) {
            log.error("Error deleting employee by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // Legacy endpoint for backward compatibility
    @DeleteMapping()
    public Response<Boolean> deleteEmployee(@RequestBody DeleteMockEmployeeInput input) {
        log.info("Deleting employee by name (legacy): {}", input != null ? input.getName() : "null");

        // Manual validation
        if (input == null) {
            log.warn("Received null delete input");
            return Response.error("Invalid input: null");
        }

        Set<ConstraintViolation<DeleteMockEmployeeInput>> violations = validator.validate(input);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for employee deletion: {}", violations);
            return Response.error("Validation failed");
        }

        try {
            final boolean deleted = mockEmployeeService.delete(input);
            log.info("Legacy delete operation result for {}: {}", input.getName(), deleted);
            return Response.handledWith(deleted);
        } catch (Exception e) {
            log.error("Error in legacy delete operation for: {}", input.getName(), e);
            return Response.error("Failed to delete employee");
        }
    }
}
