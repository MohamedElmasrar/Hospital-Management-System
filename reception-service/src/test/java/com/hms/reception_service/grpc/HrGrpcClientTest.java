package com.hms.reception_service.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hms.grpc.hr.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HrGrpcClientTest {

  @Mock private HrGrpcServiceGrpc.HrGrpcServiceBlockingStub blockingStub;

  private HrGrpcClient hrGrpcClient;

  @BeforeEach
  void setUp() {
    hrGrpcClient = new HrGrpcClient(blockingStub);
  }

  @Test
  void validateDepartment_whenExists_returnsTrue() {
    UUID deptId = UUID.randomUUID();
    ValidateDepartmentResponse response =
        ValidateDepartmentResponse.newBuilder()
            .setExists(true)
            .setDepartmentId(deptId.toString())
            .setName("Emergency")
            .build();

    when(blockingStub.validateDepartment(any(ValidateDepartmentRequest.class)))
        .thenReturn(response);

    boolean exists = hrGrpcClient.validateDepartment(deptId);
    assertThat(exists).isTrue();
  }

  @Test
  void validateDepartment_whenNotExists_returnsFalse() {
    UUID deptId = UUID.randomUUID();
    ValidateDepartmentResponse response =
        ValidateDepartmentResponse.newBuilder()
            .setExists(false)
            .setDepartmentId(deptId.toString())
            .build();

    when(blockingStub.validateDepartment(any(ValidateDepartmentRequest.class)))
        .thenReturn(response);

    boolean exists = hrGrpcClient.validateDepartment(deptId);
    assertThat(exists).isFalse();
  }

  @Test
  void getDepartment_whenNotFound_returnsEmpty() {
    UUID deptId = UUID.randomUUID();
    when(blockingStub.getDepartment(any(GetDepartmentRequest.class)))
        .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

    Optional<DepartmentResponse> result = hrGrpcClient.getDepartment(deptId);
    assertThat(result).isEmpty();
  }

  @Test
  void validateEmployee_whenActive_returnsTrue() {
    UUID empId = UUID.randomUUID();
    ValidateEmployeeResponse response =
        ValidateEmployeeResponse.newBuilder()
            .setExists(true)
            .setActive(true)
            .setFullName("John Doe")
            .build();

    when(blockingStub.validateEmployee(any(ValidateEmployeeRequest.class))).thenReturn(response);

    boolean valid = hrGrpcClient.validateEmployee(empId);
    assertThat(valid).isTrue();
  }

  @Test
  void validatePhysician_whenExists_returnsTrue() {
    UUID physicianId = UUID.randomUUID();
    ValidatePhysicianResponse response =
        ValidatePhysicianResponse.newBuilder()
            .setExists(true)
            .setFullName("Dr. Gregory House")
            .setLicenseNumber("DOC-12345")
            .build();

    when(blockingStub.validatePhysician(any(ValidatePhysicianRequest.class))).thenReturn(response);

    boolean valid = hrGrpcClient.validatePhysician(physicianId);
    assertThat(valid).isTrue();
  }
}
