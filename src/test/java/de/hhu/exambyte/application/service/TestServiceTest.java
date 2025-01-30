package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.entity.TestEntity;
import de.hhu.exambyte.infrastructure.persistence.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestServiceTest {

    @Mock
    private TestRepository testRepository;

    private TestService testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testService = new TestService(testRepository);
    }

    @org.junit.jupiter.api.Test
    void createTest() {
        String name = "Test 1";
        Test.TestStatus status = Test.TestStatus.ACTIVE;
        TestEntity testEntity = new TestEntity(name, 0, null, status);
        when(testRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        Test result = testService.createTest(name, status);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(status, result.getStatus());
        verify(testRepository).save(any(TestEntity.class));
    }

    @org.junit.jupiter.api.Test
    void getAllTests() {
        TestEntity test1 = new TestEntity("Test 1", 0, null, Test.TestStatus.ACTIVE);
        TestEntity test2 = new TestEntity("Test 2", 0, null, Test.TestStatus.COMPLETED);
        when(testRepository.findAll()).thenReturn(Arrays.asList(test1, test2));

        List<Test> result = testService.getAllTests();

        assertEquals(2, result.size());
        assertEquals("Test 1", result.get(0).getName());
        assertEquals("Test 2", result.get(1).getName());
        verify(testRepository).findAll();
    }

    @org.junit.jupiter.api.Test
    void getTestsForStudents() {
        TestEntity activeTest = new TestEntity("Active Test", 0, null, Test.TestStatus.ACTIVE);
        TestEntity inProgressTest = new TestEntity("In Progress Test", 0, null, Test.TestStatus.IN_PROGRESS);
        TestEntity completedTest = new TestEntity("Completed Test", 0, null, Test.TestStatus.COMPLETED);
        when(testRepository.findAll()).thenReturn(Arrays.asList(activeTest, inProgressTest, completedTest));

        List<Test> result = testService.getTestsForStudents();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(
                test -> test.getStatus() == Test.TestStatus.ACTIVE || test.getStatus() == Test.TestStatus.IN_PROGRESS));
    }

    @org.junit.jupiter.api.Test
    void getTestsForOrganizers() {
        TestEntity test1 = new TestEntity("Test 1", 0, null, Test.TestStatus.ACTIVE);
        TestEntity test2 = new TestEntity("Test 2", 0, null, Test.TestStatus.COMPLETED);
        when(testRepository.findAll()).thenReturn(Arrays.asList(test1, test2));

        List<Test> result = testService.getTestsForOrganizers();

        assertEquals(2, result.size());
        verify(testRepository).findAll();
    }

    @org.junit.jupiter.api.Test
    void getTestsForCorrectors() {

    }

    @org.junit.jupiter.api.Test
    void getTestById() {
        int id = 1;
        TestEntity testEntity = new TestEntity("Test 1", 0, null, Test.TestStatus.ACTIVE);
        when(testRepository.findById(id)).thenReturn(Optional.of(testEntity));

        Test result = testService.getTestById(id);

        assertNotNull(result);
        assertEquals("Test 1", result.getName());
        verify(testRepository).findById(id);
    }

    @org.junit.jupiter.api.Test
    void getTestById_notFound() {
        int id = 1;
        when(testRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> testService.getTestById(id));
        verify(testRepository).findById(id);
    }
}
