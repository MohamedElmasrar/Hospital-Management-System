package com.hms.hr_service.grpc;

import com.hms.grpc.hr.DepartmentResponse;
import com.hms.grpc.hr.EmployeeResponse;
import com.hms.grpc.hr.GetDepartmentRequest;
import com.hms.grpc.hr.GetEmployeeRequest;
import com.hms.grpc.hr.HrGrpcServiceGrpc;
import com.hms.grpc.hr.ValidateDepartmentRequest;
import com.hms.grpc.hr.ValidateDepartmentResponse;
import com.hms.grpc.hr.ValidateEmployeeRequest;
import com.hms.grpc.hr.ValidateEmployeeResponse;
import com.hms.grpc.hr.ValidatePhysicianRequest;
import com.hms.grpc.hr.ValidatePhysicianResponse;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.model.Physician;
import com.hms.hr_service.repository.DepartmentRepository;
import com.hms.hr_service.repository.EmployeeAssignmentRepository;
import com.hms.hr_service.repository.EmployeeRepository;
import com.hms.hr_service.repository.PhysicianRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrGrpcServiceImpl extends HrGrpcServiceGrpc.HrGrpcServiceImplBase {

  private final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final EmployeeAssignmentRepository employeeAssignmentRepository;
  private final PhysicianRepository physicianRepository;

  @Override
  public void getDepartment(
      GetDepartmentRequest request, StreamObserver<DepartmentResponse> responseObserver) {
    try {
      UUID departmentId = UUID.fromString(request.getDepartmentId());
      Optional<Department> optDept = departmentRepository.findById(departmentId);
      if (optDept.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("Department not found with id: " + request.getDepartmentId())
                .asRuntimeException());
        return;
      }
      Department dept = optDept.get();
      DepartmentResponse response =
          DepartmentResponse.newBuilder()
              .setId(dept.getId().toString())
              .setName(dept.getName() != null ? dept.getName() : "")
              .setDescription(dept.getDescription() != null ? dept.getDescription() : "")
              .setMedicalOrganizationId(
                  dept.getMedicalOrganization() != null
                          && dept.getMedicalOrganization().getId() != null
                      ? dept.getMedicalOrganization().getId().toString()
                      : "")
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid department UUID format: " + request.getDepartmentId())
              .asRuntimeException());
    } catch (Exception e) {
      log.error("Error retrieving department: ", e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void validateDepartment(
      ValidateDepartmentRequest request,
      StreamObserver<ValidateDepartmentResponse> responseObserver) {
    try {
      UUID departmentId = UUID.fromString(request.getDepartmentId());
      Optional<Department> optDept = departmentRepository.findById(departmentId);
      ValidateDepartmentResponse response =
          ValidateDepartmentResponse.newBuilder()
              .setExists(optDept.isPresent())
              .setDepartmentId(request.getDepartmentId())
              .setName(optDept.map(Department::getName).orElse(""))
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      ValidateDepartmentResponse response =
          ValidateDepartmentResponse.newBuilder()
              .setExists(false)
              .setDepartmentId(request.getDepartmentId())
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error validating department: ", e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void getEmployee(
      GetEmployeeRequest request, StreamObserver<EmployeeResponse> responseObserver) {
    try {
      UUID employeeId = UUID.fromString(request.getEmployeeId());
      Optional<Employee> optEmp = employeeRepository.findById(employeeId);
      if (optEmp.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("Employee not found with id: " + request.getEmployeeId())
                .asRuntimeException());
        return;
      }
      Employee emp = optEmp.get();
      List<EmployeeAssignment> assignments =
          employeeAssignmentRepository.findByEmployeeId(employeeId);
      List<String> deptIds =
          assignments.stream()
              .filter(a -> a.getDepartment() != null && a.getDepartment().getId() != null)
              .map(a -> a.getDepartment().getId().toString())
              .distinct()
              .toList();

      String fullName =
          ((emp.getGivenName() != null ? emp.getGivenName() : "")
                  + (emp.getFamilyName() != null ? " " + emp.getFamilyName() : ""))
              .trim();

      EmployeeResponse response =
          EmployeeResponse.newBuilder()
              .setId(emp.getId().toString())
              .setIdentifier(emp.getIdentifier() != null ? emp.getIdentifier() : "")
              .setFullName(fullName)
              .setStatus(emp.getEmploymentStatus() != null ? emp.getEmploymentStatus().name() : "")
              .addAllDepartmentIds(deptIds)
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid employee UUID format: " + request.getEmployeeId())
              .asRuntimeException());
    } catch (Exception e) {
      log.error("Error retrieving employee: ", e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void validateEmployee(
      ValidateEmployeeRequest request, StreamObserver<ValidateEmployeeResponse> responseObserver) {
    try {
      UUID employeeId = UUID.fromString(request.getEmployeeId());
      Optional<Employee> optEmp = employeeRepository.findById(employeeId);
      if (optEmp.isEmpty()) {
        ValidateEmployeeResponse response =
            ValidateEmployeeResponse.newBuilder().setExists(false).setActive(false).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return;
      }
      Employee emp = optEmp.get();
      String fullName =
          ((emp.getGivenName() != null ? emp.getGivenName() : "")
                  + (emp.getFamilyName() != null ? " " + emp.getFamilyName() : ""))
              .trim();
      boolean isActive =
          emp.getEmploymentStatus() != null
              && "ACTIVE".equalsIgnoreCase(emp.getEmploymentStatus().name());

      ValidateEmployeeResponse response =
          ValidateEmployeeResponse.newBuilder()
              .setExists(true)
              .setActive(isActive)
              .setFullName(fullName)
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      ValidateEmployeeResponse response =
          ValidateEmployeeResponse.newBuilder().setExists(false).setActive(false).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error validating employee: ", e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void validatePhysician(
      ValidatePhysicianRequest request,
      StreamObserver<ValidatePhysicianResponse> responseObserver) {
    try {
      UUID physicianId = UUID.fromString(request.getPhysicianId());
      Optional<Physician> optPhys = physicianRepository.findById(physicianId);
      if (optPhys.isEmpty()) {
        ValidatePhysicianResponse response =
            ValidatePhysicianResponse.newBuilder().setExists(false).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return;
      }
      Physician phys = optPhys.get();
      Employee emp = phys.getEmployee();
      String fullName =
          emp != null
              ? ((emp.getGivenName() != null ? emp.getGivenName() : "")
                      + (emp.getFamilyName() != null ? " " + emp.getFamilyName() : ""))
                  .trim()
              : "";

      ValidatePhysicianResponse response =
          ValidatePhysicianResponse.newBuilder()
              .setExists(true)
              .setFullName(fullName)
              .setLicenseNumber(phys.getLicenseNumber() != null ? phys.getLicenseNumber() : "")
              .setUserId(emp != null && emp.getUserId() != null ? emp.getUserId().toString() : "")
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      ValidatePhysicianResponse response =
          ValidatePhysicianResponse.newBuilder().setExists(false).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error validating physician: ", e);
      responseObserver.onError(
          Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
  }
}
