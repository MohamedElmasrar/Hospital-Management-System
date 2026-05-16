package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.cashdesk.CashDeskResponseDTO;
import com.hms.billing_service.dto.cashdesk.CreateCashDeskRequestDTO;
import com.hms.billing_service.dto.cashdesk.UpdateCashDeskRequestDTO;
import com.hms.billing_service.exception.CashDeskNotFoundException;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.grpc.HrGrpcClient;
import com.hms.billing_service.mapper.CashDeskMapper;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.repository.CashDeskRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashDeskService {

  private final CashDeskRepository cashDeskRepository;
  private final CashDeskMapper cashDeskMapper;
  private final HrGrpcClient hrGrpcClient;

  @Transactional
  public CashDeskResponseDTO create(CreateCashDeskRequestDTO request) {
    if (cashDeskRepository.existsByDeskCode(request.deskCode())) {
      throw new ResourceAlreadyExistsException(
          "Cash desk already exists with code: " + request.deskCode());
    }

    if (!hrGrpcClient.validateDepartment(request.departmentId())) {
      throw new InvalidOperationException(
          "Department does not exist with ID: " + request.departmentId());
    }

    CashDesk cashDesk = cashDeskMapper.createCashDeskRequestDTOToCashDesk(request);
    cashDesk.setDepartmentId(request.departmentId());
    cashDesk.setSection(request.section());
    CashDesk saved = cashDeskRepository.save(cashDesk);
    return toResponseDTO(saved);
  }

  @Transactional
  public CashDeskResponseDTO update(UUID id, UpdateCashDeskRequestDTO request) {
    CashDesk cashDesk =
        cashDeskRepository
            .findById(id)
            .orElseThrow(() -> new CashDeskNotFoundException("Cash desk not found with ID: " + id));

    if (request.deskCode() != null
        && !request.deskCode().equals(cashDesk.getDeskCode())
        && cashDeskRepository.existsByDeskCode(request.deskCode())) {
      throw new ResourceAlreadyExistsException(
          "Cash desk already exists with code: " + request.deskCode());
    }

    if (request.departmentId() != null
        && !request.departmentId().equals(cashDesk.getDepartmentId())) {
      if (!hrGrpcClient.validateDepartment(request.departmentId())) {
        throw new InvalidOperationException(
            "Department does not exist with ID: " + request.departmentId());
      }
      cashDesk.setDepartmentId(request.departmentId());
    }

    if (request.section() != null) {
      cashDesk.setSection(request.section());
    }

    cashDeskMapper.updateCashDeskFromDTO(request, cashDesk);
    CashDesk updated = cashDeskRepository.save(cashDesk);
    return toResponseDTO(updated);
  }

  @Transactional(readOnly = true)
  public CashDeskResponseDTO getById(UUID id) {
    CashDesk cashDesk =
        cashDeskRepository
            .findById(id)
            .orElseThrow(() -> new CashDeskNotFoundException("Cash desk not found with ID: " + id));
    return toResponseDTO(cashDesk);
  }

  @Transactional(readOnly = true)
  public CashDeskResponseDTO getByDeskCode(String deskCode) {
    CashDesk cashDesk =
        cashDeskRepository
            .findByDeskCode(deskCode)
            .orElseThrow(
                () -> new CashDeskNotFoundException("Cash desk not found with code: " + deskCode));
    return toResponseDTO(cashDesk);
  }

  @Transactional(readOnly = true)
  public PageResponse<CashDeskResponseDTO> getByDepartment(UUID departmentId, Pageable pageable) {
    return PageResponse.from(
        cashDeskRepository.findByDepartmentId(departmentId, pageable).map(this::toResponseDTO));
  }

  @Transactional(readOnly = true)
  public PageResponse<CashDeskResponseDTO> getAll(Pageable pageable) {
    return PageResponse.from(cashDeskRepository.findAll(pageable).map(this::toResponseDTO));
  }

  @Transactional
  public void delete(UUID id) {
    CashDesk cashDesk =
        cashDeskRepository
            .findById(id)
            .orElseThrow(() -> new CashDeskNotFoundException("Cash desk not found with ID: " + id));
    cashDesk.setIsActive(false);
    cashDeskRepository.save(cashDesk);
  }

  private CashDeskResponseDTO toResponseDTO(CashDesk cashDesk) {
    CashDeskResponseDTO dto = cashDeskMapper.cashDeskToCashDeskResponseDTO(cashDesk);
    String deptName =
        hrGrpcClient
            .getDepartment(cashDesk.getDepartmentId())
            .map(com.hms.grpc.hr.DepartmentResponse::getName)
            .orElse(null);
    return new CashDeskResponseDTO(
        dto.id(),
        dto.deskCode(),
        dto.departmentId(),
        deptName,
        cashDesk.getSection(),
        dto.isActive(),
        dto.dateCreated(),
        dto.dateModified());
  }
}
