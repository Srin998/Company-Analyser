package com.company.analyzer;

import com.company.analyzer.model.Employee;
import com.company.analyzer.service.CsvReaderService;
import com.company.analyzer.service.OrganizationalAnalyzer;
import com.company.analyzer.service.OrganizationalAnalyzer.*;

import java.io.IOException;
import java.util.List;

/**
 * Main application to analyze company organizational structure.
 * 
 * Usage: java -jar company-analyzer.jar <path-to-csv-file>
 */
public class CompanyAnalyzerApp {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar company-analyzer.jar <path-to-csv-file>");
            System.exit(1);
        }

        String filePath = args[0];
        
        try {
            // Read employees from CSV
            CsvReaderService csvReader = new CsvReaderService();
            List<Employee> employees = csvReader.readEmployees(filePath);
            
            if (employees.isEmpty()) {
                System.out.println("No employees found in the file.");
                return;
            }

            System.out.println("Analyzing organizational structure for " + employees.size() + " employees...");
            System.out.println();

            // Analyze organizational structure
            OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
            
            // Analyze salaries
            SalaryAnalysisReport salaryReport = analyzer.analyzeSalaries();
            printSalaryReport(salaryReport);
            
            // Analyze reporting lines
            ReportingLineAnalysisReport reportingLineReport = analyzer.analyzeReportingLines();
            printReportingLineReport(reportingLineReport);

            // Summary
            System.out.println();
            if (!salaryReport.hasIssues() && !reportingLineReport.hasIssues()) {
                System.out.println("No issues found. Organizational structure looks good!");
            } else {
                System.out.println("Analysis complete. Please review the issues above.");
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing CSV: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printSalaryReport(SalaryAnalysisReport report) {
        // Managers earning less than they should
        List<ManagerSalaryIssue> underpaid = report.getUnderpaidManagers();
        if (!underpaid.isEmpty()) {
            System.out.println("MANAGERS EARNING LESS THAN THEY SHOULD:");
            System.out.println("========================================");
            for (ManagerSalaryIssue issue : underpaid) {
                Employee manager = issue.getManager();
                System.out.printf("%s %s (ID: %s)%n", 
                    manager.getFirstName(), manager.getLastName(), manager.getId());
                System.out.printf("  Current salary: $%.2f%n", manager.getSalary());
                System.out.printf("  Avg subordinate salary: $%.2f%n", issue.getAvgSubordinateSalary());
                System.out.printf("  Should earn at least: $%.2f%n", 
                    issue.getAvgSubordinateSalary() * 1.20);
                System.out.printf("  Short by: $%.2f%n", issue.getDifference());
                System.out.println();
            }
        } else {
            System.out.println("No managers earning less than they should.");
            System.out.println();
        }

        // Managers earning more than they should
        List<ManagerSalaryIssue> overpaid = report.getOverpaidManagers();
        if (!overpaid.isEmpty()) {
            System.out.println("MANAGERS EARNING MORE THAN THEY SHOULD:");
            System.out.println("========================================");
            for (ManagerSalaryIssue issue : overpaid) {
                Employee manager = issue.getManager();
                System.out.printf("%s %s (ID: %s)%n", 
                    manager.getFirstName(), manager.getLastName(), manager.getId());
                System.out.printf("  Current salary: $%.2f%n", manager.getSalary());
                System.out.printf("  Avg subordinate salary: $%.2f%n", issue.getAvgSubordinateSalary());
                System.out.printf("  Should earn at most: $%.2f%n", 
                    issue.getAvgSubordinateSalary() * 1.50);
                System.out.printf("  Over by: $%.2f%n", issue.getDifference());
                System.out.println();
            }
        } else {
            System.out.println("No managers earning more than they should.");
            System.out.println();
        }
    }

    private static void printReportingLineReport(ReportingLineAnalysisReport report) {
        List<ReportingLineIssue> issues = report.getIssues();
        
        if (!issues.isEmpty()) {
            System.out.println("EMPLOYEES WITH REPORTING LINES TOO LONG:");
            System.out.println("=========================================");
            for (ReportingLineIssue issue : issues) {
                Employee employee = issue.getEmployee();
                System.out.printf("%s %s (ID: %s)%n", 
                    employee.getFirstName(), employee.getLastName(), employee.getId());
                System.out.printf("  Reporting line length: %d managers%n", 
                    issue.getReportingLineLength());
                System.out.printf("  Maximum allowed: 4 managers%n");
                System.out.printf("  Too long by: %d manager(s)%n", issue.getExcess());
                System.out.println();
            }
        } else {
            System.out.println("No employees with reporting lines too long.");
            System.out.println();
        }
    }
}
