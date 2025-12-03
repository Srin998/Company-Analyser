package com.company.analyzer.service;

import com.company.analyzer.model.Employee;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to analyze organizational structure and identify issues.
 */
public class OrganizationalAnalyzer {

    private static final double MIN_MANAGER_SALARY_RATIO = 1.20; // 20% more
    private static final double MAX_MANAGER_SALARY_RATIO = 1.50; // 50% more
    private static final int MAX_REPORTING_LINE_LENGTH = 4;

    private final Map<String, Employee> employeeMap;
    private final Map<String, List<Employee>> subordinatesMap;

    public OrganizationalAnalyzer(List<Employee> employees) {
        this.employeeMap = new HashMap<>();
        this.subordinatesMap = new HashMap<>();
        
        // Build employee lookup map
        for (Employee employee : employees) {
            employeeMap.put(employee.getId(), employee);
        }
        
        // Build subordinates map
        for (Employee employee : employees) {
            if (!employee.isCEO()) {
                subordinatesMap.computeIfAbsent(employee.getManagerId(), k -> new ArrayList<>())
                              .add(employee);
            }
        }
    }

    /**
     * Analyzes salary issues and returns a report.
     */
    public SalaryAnalysisReport analyzeSalaries() {
        List<ManagerSalaryIssue> underpaidManagers = new ArrayList<>();
        List<ManagerSalaryIssue> overpaidManagers = new ArrayList<>();

        for (Map.Entry<String, List<Employee>> entry : subordinatesMap.entrySet()) {
            String managerId = entry.getKey();
            List<Employee> subordinates = entry.getValue();
            
            Employee manager = employeeMap.get(managerId);
            if (manager == null) {
                // Invalid data: manager ID not found, skip
                continue;
            }

            double avgSubordinateSalary = calculateAverageSalary(subordinates);
            double minExpectedSalary = avgSubordinateSalary * MIN_MANAGER_SALARY_RATIO;
            double maxExpectedSalary = avgSubordinateSalary * MAX_MANAGER_SALARY_RATIO;

            if (manager.getSalary() < minExpectedSalary) {
                double shortfall = minExpectedSalary - manager.getSalary();
                underpaidManagers.add(new ManagerSalaryIssue(
                    manager, avgSubordinateSalary, shortfall));
            } else if (manager.getSalary() > maxExpectedSalary) {
                double excess = manager.getSalary() - maxExpectedSalary;
                overpaidManagers.add(new ManagerSalaryIssue(
                    manager, avgSubordinateSalary, excess));
            }
        }

        return new SalaryAnalysisReport(underpaidManagers, overpaidManagers);
    }

    /**
     * Analyzes reporting line lengths and returns employees with too long lines.
     */
    public ReportingLineAnalysisReport analyzeReportingLines() {
        List<ReportingLineIssue> issues = new ArrayList<>();

        for (Employee employee : employeeMap.values()) {
            if (employee.isCEO()) {
                continue; // CEO has no reporting line
            }

            int reportingLineLength = calculateReportingLineLength(employee);
            
            if (reportingLineLength > MAX_REPORTING_LINE_LENGTH) {
                int excess = reportingLineLength - MAX_REPORTING_LINE_LENGTH;
                issues.add(new ReportingLineIssue(employee, reportingLineLength, excess));
            }
        }

        return new ReportingLineAnalysisReport(issues);
    }

    /**
     * Calculates the number of managers between an employee and the CEO.
     */
    private int calculateReportingLineLength(Employee employee) {
        int length = 0;
        String currentManagerId = employee.getManagerId();
        Set<String> visited = new HashSet<>(); // To detect cycles
        
        while (currentManagerId != null && !currentManagerId.isEmpty()) {
            if (visited.contains(currentManagerId)) {
                // Cycle detected - treat as invalid structure
                break;
            }
            visited.add(currentManagerId);
            
            Employee manager = employeeMap.get(currentManagerId);
            if (manager == null) {
                // Invalid manager reference
                break;
            }
            
            length++;
            currentManagerId = manager.getManagerId();
        }
        
        return length;
    }

    private double calculateAverageSalary(List<Employee> employees) {
        return employees.stream()
                       .mapToDouble(Employee::getSalary)
                       .average()
                       .orElse(0.0);
    }

    /**
     * Represents a manager with salary issues.
     */
    public static class ManagerSalaryIssue {
        private final Employee manager;
        private final double avgSubordinateSalary;
        private final double difference;

        public ManagerSalaryIssue(Employee manager, double avgSubordinateSalary, double difference) {
            this.manager = manager;
            this.avgSubordinateSalary = avgSubordinateSalary;
            this.difference = difference;
        }

        public Employee getManager() {
            return manager;
        }

        public double getAvgSubordinateSalary() {
            return avgSubordinateSalary;
        }

        public double getDifference() {
            return difference;
        }
    }

    /**
     * Report of salary analysis results.
     */
    public static class SalaryAnalysisReport {
        private final List<ManagerSalaryIssue> underpaidManagers;
        private final List<ManagerSalaryIssue> overpaidManagers;

        public SalaryAnalysisReport(List<ManagerSalaryIssue> underpaidManagers, 
                                   List<ManagerSalaryIssue> overpaidManagers) {
            this.underpaidManagers = underpaidManagers;
            this.overpaidManagers = overpaidManagers;
        }

        public List<ManagerSalaryIssue> getUnderpaidManagers() {
            return underpaidManagers;
        }

        public List<ManagerSalaryIssue> getOverpaidManagers() {
            return overpaidManagers;
        }

        public boolean hasIssues() {
            return !underpaidManagers.isEmpty() || !overpaidManagers.isEmpty();
        }
    }

    /**
     * Represents an employee with a reporting line issue.
     */
    public static class ReportingLineIssue {
        private final Employee employee;
        private final int reportingLineLength;
        private final int excess;

        public ReportingLineIssue(Employee employee, int reportingLineLength, int excess) {
            this.employee = employee;
            this.reportingLineLength = reportingLineLength;
            this.excess = excess;
        }

        public Employee getEmployee() {
            return employee;
        }

        public int getReportingLineLength() {
            return reportingLineLength;
        }

        public int getExcess() {
            return excess;
        }
    }

    /**
     * Report of reporting line analysis results.
     */
    public static class ReportingLineAnalysisReport {
        private final List<ReportingLineIssue> issues;

        public ReportingLineAnalysisReport(List<ReportingLineIssue> issues) {
            this.issues = issues;
        }

        public List<ReportingLineIssue> getIssues() {
            return issues;
        }

        public boolean hasIssues() {
            return !issues.isEmpty();
        }
    }
}
