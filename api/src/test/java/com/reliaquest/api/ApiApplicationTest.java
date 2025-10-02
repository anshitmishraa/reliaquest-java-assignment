package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"spring.main.web-application-type=none"})
class ApiApplicationTest {

    @Test
    @DisplayName("Should have main method that calls SpringApplication.run")
    void shouldHaveMainMethodThatCallsSpringApplicationRun() {
        // Given - ApiApplication class

        // When - checking if main method exists
        try {
            var mainMethod = ApiApplication.class.getDeclaredMethod("main", String[].class);

            // Then - main method should exist and be public static
            assertThat(mainMethod).isNotNull();
            assertThat(mainMethod.getModifiers()).isEqualTo(9); // public static = 1 + 8 = 9
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist", e);
        }
    }

    @Test
    @DisplayName("Should be annotated with @SpringBootApplication")
    void shouldBeAnnotatedWithSpringBootApplication() {
        // When - checking for SpringBootApplication annotation
        boolean hasSpringBootApplicationAnnotation = ApiApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class);

        // Then - annotation should be present
        assertThat(hasSpringBootApplicationAnnotation).isTrue();
    }

    @Test
    @DisplayName("Should call SpringApplication.run with correct parameters")
    void shouldCallSpringApplicationRunWithCorrectParameters() {
        // Given - mocked SpringApplication
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {

            // When - calling main method
            String[] args = {"--spring.main.web-application-type=none"};
            ApiApplication.main(args);

            // Then - SpringApplication.run should be called with correct parameters
            mockedSpringApplication.verify(() -> SpringApplication.run(ApiApplication.class, args));
        }
    }

    @Test
    @DisplayName("Should be a valid Spring Boot application class")
    void shouldBeValidSpringBootApplicationClass() {
        // Given - ApiApplication class

        // When - checking class properties
        String className = ApiApplication.class.getSimpleName();
        String packageName = ApiApplication.class.getPackage().getName();

        // Then - should have correct naming and package
        assertThat(className).isEqualTo("ApiApplication");
        assertThat(packageName).isEqualTo("com.reliaquest.api");
    }

    @Test
    @DisplayName("Should have public constructor")
    void shouldHavePublicConstructor() {
        // When - creating instance of ApiApplication
        try {
            ApiApplication application = new ApiApplication();

            // Then - instance should be created successfully
            assertThat(application).isNotNull();
        } catch (Exception e) {
            throw new AssertionError("Should be able to create ApiApplication instance", e);
        }
    }
}
