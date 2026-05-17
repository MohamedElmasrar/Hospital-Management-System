package com.hms.reception_service.grpc;

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
import com.hms.reception_service.exception.InvalidOperationException;
import io.grpc.StatusRuntimeException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrGrpcClient {

  private final HrGrpcServiceGrpc.HrGrpcServiceBlockingStub hrGrpcBlockingStub;

  public boolean validateDepartment(UUID departmentId) {
    if (departmentId == null) {
      return false;
    }
    try {
      ValidateDepartmentResponse response =
          hrGrpcBlockingStub.validateDepartment(
              ValidateDepartmentRequest.newBuilder()
                  .setDepartmentId(departmentId.toString())
                  .build());
      return response.getExists();
    } catch (StatusRuntimeException e) {
      log.error("Failed to validate department via gRPC: {}", e.getStatus());
      throw new InvalidOperationException("HR service communication error: " + e.getMessage());
    }
  }

  public Optional<DepartmentResponse> getDepartment(UUID departmentId) {
    if (departmentId == null) {
      return Optional.empty();
    }
    try {
      DepartmentResponse response =
          hrGrpcBlockingStub.getDepartment(
              GetDepartmentRequest.newBuilder().setDepartmentId(departmentId.toString()).build());
      return Optional.of(response);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
        return Optional.empty();
      }
      log.error("Failed to get department via gRPC: {}", e.getStatus());
      throw new InvalidOperationException("HR service communication error: " + e.getMessage());
    }
  }

  public boolean validateEmployee(UUID employeeId) {
    if (employeeId == null) {
      return false;
    }
    try {
      ValidateEmployeeResponse response =
          hrGrpcBlockingStub.validateEmployee(
              ValidateEmployeeRequest.newBuilder().setEmployeeId(employeeId.toString()).build());
      return response.getExists() && response.getActive();
    } catch (StatusRuntimeException e) {
      log.error("Failed to validate employee via gRPC: {}", e.getStatus());
      throw new InvalidOperationException("HR service communication error: " + e.getMessage());
    }
  }

  public Optional<EmployeeResponse> getEmployee(UUID employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    try {
      EmployeeResponse response =
          hrGrpcBlockingStub.getEmployee(
              GetEmployeeRequest.newBuilder().setEmployeeId(employeeId.toString()).build());
      return Optional.of(response);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
        return Optional.empty();
      }
      log.error("Failed to get employee via gRPC: {}", e.getStatus());
      throw new InvalidOperationException("HR service communication error: " + e.getMessage());
    }
  }

  public boolean validatePhysician(UUID physicianId) {
    if (physicianId == null) {
      return false;
    }
    try {
      ValidatePhysicianResponse response =
          hrGrpcBlockingStub.validatePhysician(
              ValidatePhysicianRequest.newBuilder().setPhysicianId(physicianId.toString()).build());
      return response.getExists();
    } catch (StatusRuntimeException e) {
      log.error("Failed to validate physician via gRPC: {}", e.getStatus());
      throw new InvalidOperationException("HR service communication error: " + e.getMessage());
    }
  }
}
