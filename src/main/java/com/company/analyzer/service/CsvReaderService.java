package com.company.analyzer.service;

import com.company.analyzer.model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to read and parse employee data from CSV file.
 */
public class CsvReaderService {

    /**
     * Reads employees from a CSV file.
     * 
     * Assumptions:
     * - CSV file has a header row
     * - Fields are: Id,firstName,lastName,salary,managerId
     * - Empty managerId indicates CEO
     * - Salary is a valid numeric value
     * 
     * @param filePath path to the CSV file
     * @return list of employees
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if CSV format is invalid
     */
    public List<Employee> readEmployees(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip header
            
            if (line == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Employee employee = parseLine(line);
                    employees.add(employee);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Invalid CSV format at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }

        return employees;
    }

    /**
     * Parses a single CSV line into an Employee object.
     */
    private Employee parseLine(String line) {
        String[] parts = line.split(",", -1); // -1 keeps trailing empty strings
        
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                "Expected 5 fields but found " + parts.length);
        }

        String id = parts[0].trim();
        String firstName = parts[1].trim();
        String lastName = parts[2].trim();
        String salaryStr = parts[3].trim();
        String managerId = parts[4].trim();

        if (id.isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }

        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty");
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
            if (salary < 0) {
                throw new IllegalArgumentException("Salary cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid salary value: " + salaryStr);
        }

        // Empty managerId is valid (CEO case)
        return new Employee(id, firstName, lastName, salary, 
                           managerId.isEmpty() ? null : managerId);
    }
}
