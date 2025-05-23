package com.example.authservice.service;

import com.example.authservice.dto.MerchantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MerchantService {
    MerchantDto createMerchant(MerchantDto merchantDto);
    MerchantDto getMerchantById(Long id);
    Page<MerchantDto> getAllMerchants(Pageable pageable);
    Page<MerchantDto> getMerchantsByChannelId(Long channelId, Pageable pageable);
    Page<MerchantDto> searchMerchantsByName(String name, Pageable pageable);
    MerchantDto updateMerchant(Long id, MerchantDto merchantDto);
    void deleteMerchant(Long id);
}
