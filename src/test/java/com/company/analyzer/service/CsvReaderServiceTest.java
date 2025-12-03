package com.company.analyzer.service;

import com.company.analyzer.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderServiceTest {

    private final CsvReaderService csvReader = new CsvReaderService();

    @Test
    void testReadValidCsv(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                """;
        Files.writeString(csvFile, content);

        List<Employee> employees = csvReader.readEmployees(csvFile.toString());

        assertEquals(3, employees.size());
        
        Employee ceo = employees.get(0);
        assertEquals("123", ceo.getId());
        assertEquals("Joe", ceo.getFirstName());
        assertEquals("Doe", ceo.getLastName());
        assertEquals(60000.0, ceo.getSalary());
        assertTrue(ceo.isCEO());

        Employee emp1 = employees.get(1);
        assertEquals("124", emp1.getId());
        assertEquals("Martin", emp1.getFirstName());
        assertEquals("45000", emp1.getManagerId());
    }

    @Test
    void testReadCsvWithEmptyLines(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                
                124,Martin,Chekov,45000,123
                """;
        Files.writeString(csvFile, content);

        List<Employee> employees = csvReader.readEmployees(csvFile.toString());

        assertEquals(2, employees.size());
    }

    @Test
    void testEmptyFile(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("empty.csv");
        Files.writeString(csvFile, "");

        assertThrows(IllegalArgumentException.class, 
            () -> csvReader.readEmployees(csvFile.toString()));
    }

    @Test
    void testInvalidSalary(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,invalid,
                """;
        Files.writeString(csvFile, content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> csvReader.readEmployees(csvFile.toString()));
        assertTrue(ex.getMessage().contains("Invalid salary value"));
    }

    @Test
    void testNegativeSalary(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,-1000,
                """;
        Files.writeString(csvFile, content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> csvReader.readEmployees(csvFile.toString()));
        assertTrue(ex.getMessage().contains("Salary cannot be negative"));
    }

    @Test
    void testMissingFields(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe
                """;
        Files.writeString(csvFile, content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> csvReader.readEmployees(csvFile.toString()));
        assertTrue(ex.getMessage().contains("Expected 5 fields"));
    }

    @Test
    void testEmptyEmployeeId(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("employees.csv");
        String content = """
                Id,firstName,lastName,salary,managerId
                ,Joe,Doe,60000,
                """;
        Files.writeString(csvFile, content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> csvReader.readEmployees(csvFile.toString()));
        assertTrue(ex.getMessage().contains("Employee ID cannot be empty"));
    }

    @Test
    void testFileNotFound() {
        assertThrows(IOException.class,
            () -> csvReader.readEmployees("/nonexistent/file.csv"));
    }
}
