package com.example.authservice.controller;

import com.example.authservice.dto.MerchantDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantDto> createMerchant(@Valid @RequestBody MerchantDto merchantDto) {
        MerchantDto createdMerchant = merchantService.createMerchant(merchantDto);
        return new ResponseEntity<>(createdMerchant, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MerchantDto> getMerchantById(@PathVariable Long id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        return ResponseEntity.ok(merchant);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MerchantDto>> getAllMerchants(@PageableDefault(size = 20) Pageable pageable) {
        Page<MerchantDto> merchants = merchantService.getAllMerchants(pageable);
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/by-channel/{channelId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MerchantDto>> getMerchantsByChannelId(@PathVariable Long channelId, @PageableDefault(size = 20) Pageable pageable) {
        Page<MerchantDto> merchants = merchantService.getMerchantsByChannelId(channelId, pageable);
        return ResponseEntity.ok(merchants);
    }
    
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MerchantDto>> searchMerchantsByName(@RequestParam String name, @PageableDefault(size = 20) Pageable pageable) {
        Page<MerchantDto> merchants = merchantService.searchMerchantsByName(name, pageable);
        return ResponseEntity.ok(merchants);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantDto> updateMerchant(@PathVariable Long id, @Valid @RequestBody MerchantDto merchantDto) {
        MerchantDto updatedMerchant = merchantService.updateMerchant(id, merchantDto);
        return ResponseEntity.ok(updatedMerchant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Merchant deleted successfully with id: " + id));
    }
}
