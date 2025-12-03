package com.company.analyzer.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testEmployeeCreation() {
        Employee employee = new Employee("123", "John", "Doe", 50000.0, "456");
        
        assertEquals("123", employee.getId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals(50000.0, employee.getSalary());
        assertEquals("456", employee.getManagerId());
        assertEquals("John Doe", employee.getFullName());
        assertFalse(employee.isCEO());
    }

    @Test
    void testCEOEmployee() {
        Employee ceo = new Employee("1", "Jane", "Smith", 150000.0, null);
        assertTrue(ceo.isCEO());
        
        Employee ceoEmptyManager = new Employee("2", "Bob", "Jones", 150000.0, "");
        assertTrue(ceoEmptyManager.isCEO());
    }

    @Test
    void testEmployeeEquality() {
        Employee emp1 = new Employee("123", "John", "Doe", 50000.0, "456");
        Employee emp2 = new Employee("123", "Jane", "Smith", 60000.0, "789");
        Employee emp3 = new Employee("124", "John", "Doe", 50000.0, "456");
        
        assertEquals(emp1, emp2); // Same ID
        assertNotEquals(emp1, emp3); // Different ID
        assertEquals(emp1.hashCode(), emp2.hashCode());
    }

    @Test
    void testToString() {
        Employee employee = new Employee("123", "John", "Doe", 50000.0, "456");
        String str = employee.toString();
        
        assertTrue(str.contains("123"));
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("50000.0"));
    }
}
