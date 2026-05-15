package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.cashdesk.CashDeskResponseDTO;
import com.hms.billing_service.dto.cashdesk.CreateCashDeskRequestDTO;
import com.hms.billing_service.dto.cashdesk.UpdateCashDeskRequestDTO;
import com.hms.billing_service.exception.CashDeskNotFoundException;
import com.hms.billing_service.exception.DepartmentNotFoundException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.mapper.CashDeskMapper;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.Department;
import com.hms.billing_service.repository.CashDeskRepository;
import com.hms.billing_service.repository.DepartmentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashDeskService {

  private final CashDeskRepository cashDeskRepository;
  private final DepartmentRepository departmentRepository;
  private final CashDeskMapper cashDeskMapper;

  @Transactional
  public CashDeskResponseDTO create(CreateCashDeskRequestDTO request) {
    if (cashDeskRepository.existsByDeskCode(request.deskCode())) {
      throw new ResourceAlreadyExistsException(
          "Cash desk already exists with code: " + request.deskCode());
    }

    Department department =
        departmentRepository
            .findById(request.departmentId())
            .orElseThrow(
                () ->
                    new DepartmentNotFoundException(
                        "Department not found with ID: " + request.departmentId()));

    CashDesk cashDesk = cashDeskMapper.createCashDeskRequestDTOToCashDesk(request, department);
    CashDesk saved = cashDeskRepository.save(cashDesk);
    return cashDeskMapper.cashDeskToCashDeskResponseDTO(saved);
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
        && !request.departmentId().equals(cashDesk.getDepartment().getId())) {
      Department department =
          departmentRepository
              .findById(request.departmentId())
              .orElseThrow(
                  () ->
                      new DepartmentNotFoundException(
                          "Department not found with ID: " + request.departmentId()));
      cashDesk.setDepartment(department);
    }

    cashDeskMapper.updateCashDeskFromDTO(request, cashDesk);
    CashDesk updated = cashDeskRepository.save(cashDesk);
    return cashDeskMapper.cashDeskToCashDeskResponseDTO(updated);
  }

  @Transactional(readOnly = true)
  public CashDeskResponseDTO getById(UUID id) {
    CashDesk cashDesk =
        cashDeskRepository
            .findById(id)
            .orElseThrow(() -> new CashDeskNotFoundException("Cash desk not found with ID: " + id));
    return cashDeskMapper.cashDeskToCashDeskResponseDTO(cashDesk);
  }

  @Transactional(readOnly = true)
  public CashDeskResponseDTO getByDeskCode(String deskCode) {
    CashDesk cashDesk =
        cashDeskRepository
            .findByDeskCode(deskCode)
            .orElseThrow(
                () -> new CashDeskNotFoundException("Cash desk not found with code: " + deskCode));
    return cashDeskMapper.cashDeskToCashDeskResponseDTO(cashDesk);
  }

  @Transactional(readOnly = true)
  public PageResponse<CashDeskResponseDTO> getByDepartment(UUID departmentId, Pageable pageable) {
    return PageResponse.from(
        cashDeskRepository
            .findByDepartmentId(departmentId, pageable)
            .map(cashDeskMapper::cashDeskToCashDeskResponseDTO));
  }

  @Transactional(readOnly = true)
  public PageResponse<CashDeskResponseDTO> getAll(Pageable pageable) {
    return PageResponse.from(
        cashDeskRepository.findAll(pageable).map(cashDeskMapper::cashDeskToCashDeskResponseDTO));
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
}

