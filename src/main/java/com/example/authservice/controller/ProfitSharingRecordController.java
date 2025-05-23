package com.example.authservice.controller;

import com.example.authservice.dto.CreateProfitSharingRecordDto;
import com.example.authservice.dto.ProfitSharingRecordDto;
import com.example.authservice.dto.UpdateProfitSharingStatusDto;
import com.example.authservice.service.ProfitSharingRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profit-sharing-records")
@RequiredArgsConstructor
public class ProfitSharingRecordController {

    private final ProfitSharingRecordService profitSharingRecordService;

    // Helper to extract UserDetails from Authentication
    private UserDetails getCurrentUserDetails(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated or is anonymous");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        throw new AuthenticationCredentialsNotFoundException("Principal is not an instance of UserDetails");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfitSharingRecordDto> createProfitSharingRecord(@Valid @RequestBody CreateProfitSharingRecordDto recordDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ProfitSharingRecordDto createdRecord = profitSharingRecordService.createProfitSharingRecord(recordDto, currentUser);
        return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfitSharingRecordDto> getProfitSharingRecordById(@PathVariable Long id) {
        ProfitSharingRecordDto record = profitSharingRecordService.getProfitSharingRecordById(id);
        return ResponseEntity.ok(record);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfitSharingRecordDto>> getAllProfitSharingRecords(@PageableDefault(size = 20) Pageable pageable) {
        Page<ProfitSharingRecordDto> records = profitSharingRecordService.getAllProfitSharingRecords(pageable);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfitSharingRecordDto>> getProfitSharingRecordsByOrderId(@PathVariable Long orderId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfitSharingRecordDto> records = profitSharingRecordService.getProfitSharingRecordsByOrderId(orderId, pageable);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/by-channel/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfitSharingRecordDto>> getProfitSharingRecordsByChannelId(@PathVariable Long channelId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfitSharingRecordDto> records = profitSharingRecordService.getProfitSharingRecordsByChannelId(channelId, pageable);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfitSharingRecordDto>> getProfitSharingRecordsByMerchantId(@PathVariable Long merchantId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfitSharingRecordDto> records = profitSharingRecordService.getProfitSharingRecordsByMerchantId(merchantId, pageable);
        return ResponseEntity.ok(records);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfitSharingRecordDto> updateProfitSharingStatus(@PathVariable Long id, @Valid @RequestBody UpdateProfitSharingStatusDto statusDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ProfitSharingRecordDto updatedRecord = profitSharingRecordService.updateProfitSharingStatus(id, statusDto, currentUser);
        return ResponseEntity.ok(updatedRecord);
    }
}
