package com.hms.billing_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hms.billing_service.dto.cashdesk.CashDeskResponseDTO;
import com.hms.billing_service.dto.cashdesk.CreateCashDeskRequestDTO;
import com.hms.billing_service.dto.cashdesk.UpdateCashDeskRequestDTO;
import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.exception.CashDeskNotFoundException;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.grpc.HrGrpcClient;
import com.hms.billing_service.mapper.CashDeskMapper;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.repository.CashDeskRepository;
import com.hms.grpc.hr.DepartmentResponse;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CashDeskServiceTest {

  @Mock private CashDeskRepository cashDeskRepository;
  @Mock private HrGrpcClient hrGrpcClient;

  private CashDeskMapper cashDeskMapper;
  private CashDeskService cashDeskService;

  @BeforeEach
  void setUp() {
    cashDeskMapper = Mappers.getMapper(CashDeskMapper.class);
    cashDeskService = new CashDeskService(cashDeskRepository, cashDeskMapper, hrGrpcClient);
  }

  @Test
  void create_whenCodeExists_throwsException() {
    CreateCashDeskRequestDTO request =
        new CreateCashDeskRequestDTO("DESK-01", UUID.randomUUID(), BillingSectionType.RECEPTION);
    when(cashDeskRepository.existsByDeskCode("DESK-01")).thenReturn(true);

    assertThatThrownBy(() -> cashDeskService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("DESK-01");
  }

  @Test
  void create_whenDepartmentDoesNotExistInHr_throwsException() {
    UUID deptId = UUID.randomUUID();
    CreateCashDeskRequestDTO request =
        new CreateCashDeskRequestDTO("DESK-01", deptId, BillingSectionType.RECEPTION);
    when(cashDeskRepository.existsByDeskCode("DESK-01")).thenReturn(false);
    when(hrGrpcClient.validateDepartment(deptId)).thenReturn(false);

    assertThatThrownBy(() -> cashDeskService.create(request))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("Department does not exist");
  }

  @Test
  void create_success_returnsResponseDTOWithEnrichedDeptName() {
    UUID deptId = UUID.randomUUID();
    CreateCashDeskRequestDTO request =
        new CreateCashDeskRequestDTO("DESK-01", deptId, BillingSectionType.RECEPTION);

    when(cashDeskRepository.existsByDeskCode("DESK-01")).thenReturn(false);
    when(hrGrpcClient.validateDepartment(deptId)).thenReturn(true);
    when(hrGrpcClient.getDepartment(deptId))
        .thenReturn(
            Optional.of(
                DepartmentResponse.newBuilder()
                    .setId(deptId.toString())
                    .setName("Admissions Department")
                    .build()));

    CashDesk savedDesk = new CashDesk();
    savedDesk.setId(UUID.randomUUID());
    savedDesk.setDeskCode("DESK-01");
    savedDesk.setDepartmentId(deptId);
    savedDesk.setSection(BillingSectionType.RECEPTION);
    savedDesk.setIsActive(true);
    savedDesk.setDateCreated(new Date());
    savedDesk.setDateModified(new Date());

    when(cashDeskRepository.save(any(CashDesk.class))).thenReturn(savedDesk);

    CashDeskResponseDTO response = cashDeskService.create(request);

    assertThat(response).isNotNull();
    assertThat(response.deskCode()).isEqualTo("DESK-01");
    assertThat(response.departmentName()).isEqualTo("Admissions Department");
    assertThat(response.section()).isEqualTo(BillingSectionType.RECEPTION);
  }

  @Test
  void update_notFound_throwsException() {
    UUID id = UUID.randomUUID();
    UpdateCashDeskRequestDTO request = new UpdateCashDeskRequestDTO("DESK-02", null, null, null);
    when(cashDeskRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cashDeskService.update(id, request))
        .isInstanceOf(CashDeskNotFoundException.class);
  }

  @Test
  void delete_success_setsInactive() {
    UUID id = UUID.randomUUID();
    CashDesk desk = new CashDesk();
    desk.setId(id);
    desk.setIsActive(true);

    when(cashDeskRepository.findById(id)).thenReturn(Optional.of(desk));

    cashDeskService.delete(id);

    assertThat(desk.getIsActive()).isFalse();
    verify(cashDeskRepository).save(desk);
  }
}
