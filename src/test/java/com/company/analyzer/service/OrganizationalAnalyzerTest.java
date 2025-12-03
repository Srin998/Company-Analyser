package com.company.analyzer.service;

import com.company.analyzer.model.Employee;
import com.company.analyzer.service.OrganizationalAnalyzer.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationalAnalyzerTest {

    @Test
    void testManagerEarningLessThanExpected() {
        // CEO earning 60000
        // Manager earning 45000 with subordinates averaging 50000
        // Should earn at least 60000 (50000 * 1.20)
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Boss", 100000, null),
            new Employee("2", "Manager", "Underpaid", 45000, "1"),
            new Employee("3", "Dev", "One", 50000, "2"),
            new Employee("4", "Dev", "Two", 50000, "2")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        SalaryAnalysisReport report = analyzer.analyzeSalaries();

        assertEquals(1, report.getUnderpaidManagers().size());
        assertEquals(0, report.getOverpaidManagers().size());

        ManagerSalaryIssue issue = report.getUnderpaidManagers().get(0);
        assertEquals("2", issue.getManager().getId());
        assertEquals(50000.0, issue.getAvgSubordinateSalary());
        assertEquals(15000.0, issue.getDifference(), 0.01); // Should earn 60000, earns 45000
    }

    @Test
    void testManagerEarningMoreThanExpected() {
        // Manager earning 80000 with subordinates averaging 50000
        // Should earn at most 75000 (50000 * 1.50)
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Boss", 100000, null),
            new Employee("2", "Manager", "Overpaid", 80000, "1"),
            new Employee("3", "Dev", "One", 50000, "2"),
            new Employee("4", "Dev", "Two", 50000, "2")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        SalaryAnalysisReport report = analyzer.analyzeSalaries();

        assertEquals(0, report.getUnderpaidManagers().size());
        assertEquals(1, report.getOverpaidManagers().size());

        ManagerSalaryIssue issue = report.getOverpaidManagers().get(0);
        assertEquals("2", issue.getManager().getId());
        assertEquals(50000.0, issue.getAvgSubordinateSalary());
        assertEquals(5000.0, issue.getDifference(), 0.01); // Should earn 75000, earns 80000
    }

    @Test
    void testManagerWithCorrectSalary() {
        // Manager earning 60000 with subordinates averaging 50000
        // Should earn between 60000 (1.20) and 75000 (1.50) - OK!
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Boss", 100000, null),
            new Employee("2", "Manager", "Good", 60000, "1"),
            new Employee("3", "Dev", "One", 50000, "2"),
            new Employee("4", "Dev", "Two", 50000, "2")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        SalaryAnalysisReport report = analyzer.analyzeSalaries();

        assertEquals(0, report.getUnderpaidManagers().size());
        assertEquals(0, report.getOverpaidManagers().size());
        assertFalse(report.hasIssues());
    }

    @Test
    void testReportingLineTooLong() {
        // Employee with 5 managers between them and CEO
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Top", 100000, null),
            new Employee("2", "Mgr", "Level1", 90000, "1"),
            new Employee("3", "Mgr", "Level2", 80000, "2"),
            new Employee("4", "Mgr", "Level3", 70000, "3"),
            new Employee("5", "Mgr", "Level4", 60000, "4"),
            new Employee("6", "Dev", "Bottom", 50000, "5")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        ReportingLineAnalysisReport report = analyzer.analyzeReportingLines();

        assertEquals(1, report.getIssues().size());
        assertTrue(report.hasIssues());

        ReportingLineIssue issue = report.getIssues().get(0);
        assertEquals("6", issue.getEmployee().getId());
        assertEquals(5, issue.getReportingLineLength());
        assertEquals(1, issue.getExcess());
    }

    @Test
    void testReportingLineAcceptable() {
        // Employee with 4 managers - exactly at the limit
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Top", 100000, null),
            new Employee("2", "Mgr", "Level1", 90000, "1"),
            new Employee("3", "Mgr", "Level2", 80000, "2"),
            new Employee("4", "Mgr", "Level3", 70000, "3"),
            new Employee("5", "Dev", "Bottom", 50000, "4")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        ReportingLineAnalysisReport report = analyzer.analyzeReportingLines();

        assertEquals(0, report.getIssues().size());
        assertFalse(report.hasIssues());
    }

    @Test
    void testComplexOrganization() {
        // Test with the example data from requirements
        List<Employee> employees = Arrays.asList(
            new Employee("123", "Joe", "Doe", 60000, null),
            new Employee("124", "Martin", "Chekov", 45000, "123"),
            new Employee("125", "Bob", "Ronstad", 47000, "123"),
            new Employee("300", "Alice", "Hasacat", 50000, "124"),
            new Employee("305", "Brett", "Hardleaf", 34000, "300")
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        
        SalaryAnalysisReport salaryReport = analyzer.analyzeSalaries();
        ReportingLineAnalysisReport lineReport = analyzer.analyzeReportingLines();

        // Martin manages Alice (50000), should earn at least 60000, but earns 45000
        assertEquals(1, salaryReport.getUnderpaidManagers().size());
        assertEquals("124", salaryReport.getUnderpaidManagers().get(0).getManager().getId());

        // Alice manages Brett, her salary might be an issue too
        // Brett (34000) - Alice should earn 40800-51000
        // Alice earns 50000 - within range, so OK

        // No reporting lines too long (max is 3: Brett -> Alice -> Martin -> Joe)
        assertEquals(0, lineReport.getIssues().size());
    }

    @Test
    void testCEOHasNoReportingLine() {
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Boss", 100000, null)
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        ReportingLineAnalysisReport report = analyzer.analyzeReportingLines();

        assertEquals(0, report.getIssues().size());
    }

    @Test
    void testInvalidManagerReference() {
        // Employee with non-existent manager - should handle gracefully
        List<Employee> employees = Arrays.asList(
            new Employee("1", "CEO", "Boss", 100000, null),
            new Employee("2", "Dev", "Orphan", 50000, "999") // Manager doesn't exist
        );

        OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
        
        // Should not throw exception
        SalaryAnalysisReport salaryReport = analyzer.analyzeSalaries();
        ReportingLineAnalysisReport lineReport = analyzer.analyzeReportingLines();

        assertNotNull(salaryReport);
        assertNotNull(lineReport);
    }
}
