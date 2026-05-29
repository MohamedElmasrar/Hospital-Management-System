package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.Employee.AssignEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.ContactPointResponseDTO;
import com.hms.hr_service.dto.Employee.CreateContactPointRequestDTO;
import com.hms.hr_service.dto.Employee.CreateEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.EmployeeAssignmentResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeDetailResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeResponseDTO;
import com.hms.hr_service.dto.Employee.PostalAddressDTO;
import com.hms.hr_service.dto.Employee.UpdateEmployeeRequestDTO;
import com.hms.hr_service.model.ContactPoint;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.model.PostalAddress;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

  EmployeeResponseDTO employeeToEmployeeResponseDTO(Employee employee);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  Employee createEmployeeRequestDTOToEmployee(CreateEmployeeRequestDTO dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "identifier", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  void updateEmployeeFromDTO(UpdateEmployeeRequestDTO dto, @MappingTarget Employee employee);

  @Mapping(source = "preferred", target = "isPreferred")
  ContactPointResponseDTO contactPointToContactPointResponseDTO(ContactPoint cp);

  PostalAddressDTO postalAddressToPostalAddressDTO(PostalAddress address);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  PostalAddress postalAddressDTOToPostalAddress(PostalAddressDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  PostalAddress toPostalAddress(PostalAddressDTO dto, Employee employee);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  void updatePostalAddressFromDTO(PostalAddressDTO dto, @MappingTarget PostalAddress address);

  @Mapping(source = "employee.id", target = "employeeId")
  @Mapping(source = "employee.identifier", target = "employeeIdentifier")
  @Mapping(
      target = "employeeName",
      expression =
          "java(assignment.getEmployee() != null ? assignment.getEmployee().getGivenName() + \" \" + assignment.getEmployee().getFamilyName() : null)")
  @Mapping(source = "department.id", target = "departmentId")
  @Mapping(source = "department.name", target = "departmentName")
  EmployeeAssignmentResponseDTO employeeAssignmentToEmployeeAssignmentResponseDTO(
      EmployeeAssignment assignment);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  @Mapping(
      target = "contactType",
      expression =
          "java(dto.contactType() != null ? dto.contactType() : com.hms.hr_service.enums.ContactType.Work)")
  @Mapping(target = "email", source = "dto.email")
  @Mapping(target = "telephone", source = "dto.telephone")
  @Mapping(target = "preferred", constant = "true")
  ContactPoint createEmployeeRequestDTOToContactPoint(
      CreateEmployeeRequestDTO dto, Employee employee);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  @Mapping(
      target = "addressType",
      expression =
          "java(dto.addressType() != null ? dto.addressType() : com.hms.hr_service.enums.AddressType.Work)")
  @Mapping(target = "streetAddress", source = "dto.streetAddress")
  @Mapping(target = "addressLocality", source = "dto.addressLocality")
  @Mapping(target = "addressRegion", source = "dto.addressRegion")
  @Mapping(target = "postalCode", source = "dto.postalCode")
  @Mapping(target = "addressCountry", source = "dto.addressCountry")
  PostalAddress createEmployeeRequestDTOToPostalAddress(
      CreateEmployeeRequestDTO dto, Employee employee);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  @Mapping(target = "contactType", source = "dto.contactType")
  @Mapping(target = "telephone", source = "dto.telephone")
  @Mapping(target = "email", source = "dto.email")
  @Mapping(target = "preferred", source = "dto.isPreferred")
  ContactPoint toContactPoint(CreateContactPointRequestDTO dto, Employee employee);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  @Mapping(target = "department", source = "department")
  @Mapping(target = "jobTitle", source = "request.jobTitle")
  @Mapping(target = "startDate", source = "request.startDate")
  @Mapping(target = "endDate", source = "request.endDate")
  EmployeeAssignment toEmployeeAssignment(
      AssignEmployeeRequestDTO request, Employee employee, Department department);

  @Mapping(source = "employee.id", target = "id")
  @Mapping(source = "employee.identifier", target = "identifier")
  @Mapping(source = "employee.givenName", target = "givenName")
  @Mapping(source = "employee.additionalName", target = "additionalName")
  @Mapping(source = "employee.familyName", target = "familyName")
  @Mapping(source = "employee.honorificPrefix", target = "honorificPrefix")
  @Mapping(source = "employee.birthDate", target = "birthDate")
  @Mapping(source = "employee.gender", target = "gender")
  @Mapping(source = "employee.employmentStatus", target = "employmentStatus")
  @Mapping(source = "employee.dateCreated", target = "dateCreated")
  @Mapping(source = "employee.dateModified", target = "dateModified")
  @Mapping(source = "contactPoints", target = "contactPoints")
  @Mapping(source = "postalAddress", target = "postalAddress")
  @Mapping(source = "assignments", target = "assignments")
  EmployeeDetailResponseDTO toEmployeeDetailResponseDTO(
      Employee employee,
      List<ContactPoint> contactPoints,
      PostalAddress postalAddress,
      List<EmployeeAssignment> assignments);
}
