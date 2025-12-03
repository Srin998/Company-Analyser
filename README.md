# Company-Analyser

A Java command-line application to analyze company organizational structure and identify potential improvements.

## Features

The application analyzes:
1. **Manager Salary Issues**: Identifies managers who earn less than 20% or more than 50% above their direct subordinates' average salary
2. **Reporting Line Length**: Identifies employees with more than 4 managers between them and the CEO

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean package
```

## Running the Application

```bash
java -jar target/company-analyzer-1.0-SNAPSHOT.jar <path-to-csv-file>
```

Example:
```bash
java -jar target/company-analyzer-1.0-SNAPSHOT.jar employees.csv
```

## Running Tests

```bash
mvn test
```

## CSV File Format

The CSV file should have the following structure:

```
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
```

- **Id**: Unique employee identifier
- **firstName**: Employee's first name
- **lastName**: Employee's last name
- **salary**: Employee's salary (numeric value)
- **managerId**: ID of the employee's manager (empty for CEO)

## Assumptions

1. **CSV Format**: The CSV file has a header row and follows the specified format
2. **CEO Identification**: The CEO is identified by having an empty managerId field
3. **Valid Data**: Employee IDs are unique, and manager references are valid (except for graceful handling of orphaned employees)
4. **Salary Calculation**: Average subordinate salary is calculated using direct reports only
5. **Reporting Line**: Counts the number of managers between an employee and the CEO (not including the employee themselves)
6. **Cycle Detection**: If circular reporting structures are detected, the analysis stops gracefully

