package com.hms.hr_service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeShift;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WorkforceMapperTest {
  private final WorkforceMapper mapper = Mappers.getMapper(WorkforceMapper.class);

  @Test
  void shiftIncludesEmployeeAndDepartmentNames() {
    Employee employee = new Employee();
    employee.setId(UUID.randomUUID());
    employee.setGivenName("Ada");
    employee.setFamilyName("Lovelace");
    Department department = new Department();
    department.setId(UUID.randomUUID());
    department.setName("Cardiology");
    EmployeeShift shift = new EmployeeShift();
    shift.setId(UUID.randomUUID());
    shift.setEmployee(employee);
    shift.setDepartment(department);
    shift.setShiftDate(LocalDate.of(2026, 10, 30));

    var result = mapper.shift(shift);

    assertThat(result.employeeId()).isEqualTo(employee.getId());
    assertThat(result.employeeName()).isEqualTo("Ada Lovelace");
    assertThat(result.departmentName()).isEqualTo("Cardiology");
  }
}
