package com.reliaquest.server.service;

import com.reliaquest.server.config.ServerConfiguration;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockEmployeeService {

    private final Faker faker;

    @Getter
    private final List<MockEmployee> mockEmployees;

    public Optional<MockEmployee> findById(@NonNull UUID uuid) {
        return mockEmployees.stream()
                .filter(mockEmployee -> Objects.nonNull(mockEmployee.getId())
                        && mockEmployee.getId().equals(uuid))
                .findFirst();
    }

    public MockEmployee create(@NonNull CreateMockEmployeeInput input) {
        final var mockEmployee = MockEmployee.from(
                ServerConfiguration.EMAIL_TEMPLATE.formatted(
                        faker.twitter().userName().toLowerCase()),
                input);
        mockEmployees.add(mockEmployee);
        log.debug("Added employee: {}", mockEmployee);
        return mockEmployee;
    }

    public boolean delete(@NonNull DeleteMockEmployeeInput input) {
        final var mockEmployee = mockEmployees.stream()
                .filter(employee -> Objects.nonNull(employee.getName())
                        && employee.getName().equalsIgnoreCase(input.getName()))
                .findFirst();
        if (mockEmployee.isPresent()) {
            mockEmployees.remove(mockEmployee.get());
            log.debug("Removed employee: {}", mockEmployee.get());
            return true;
        }

        return false;
    }

    public boolean deleteById(@NonNull UUID id) {
        final var mockEmployee = findById(id);
        if (mockEmployee.isPresent()) {
            mockEmployees.remove(mockEmployee.get());
            log.debug("Removed employee by ID: {}", mockEmployee.get());
            return true;
        }
        log.warn("Employee with ID {} not found for deletion", id);
        return false;
    }

    public List<MockEmployee> searchByName(@NonNull String searchString) {
        log.debug("Searching employees by name containing: {}", searchString);
        if (searchString.trim().isEmpty()) {
            log.warn("Empty search string provided");
            return List.of();
        }

        final var results = mockEmployees.stream()
                .filter(employee -> Objects.nonNull(employee.getName())
                        && employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Found {} employees matching search string: {}", results.size(), searchString);
        return results;
    }

    public Optional<Integer> getHighestSalary() {
        log.debug("Finding highest salary among {} employees", mockEmployees.size());
        final var highestSalary = mockEmployees.stream()
                .filter(employee -> Objects.nonNull(employee.getSalary()))
                .mapToInt(MockEmployee::getSalary)
                .max();

        if (highestSalary.isPresent()) {
            log.debug("Highest salary found: {}", highestSalary.getAsInt());
            return Optional.of(highestSalary.getAsInt());
        } else {
            log.warn("No employees with valid salary found");
            return Optional.empty();
        }
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.debug("Finding top 10 highest earning employees");
        final var topTenNames = mockEmployees.stream()
                .filter(employee -> Objects.nonNull(employee.getSalary()) && Objects.nonNull(employee.getName()))
                .sorted(Comparator.comparing(MockEmployee::getSalary).reversed())
                .limit(10)
                .map(MockEmployee::getName)
                .collect(Collectors.toList());

        log.debug("Found {} top earning employees", topTenNames.size());
        return topTenNames;
    }
}
