package com.example.authservice.service;

import com.example.authservice.dto.CreateProfitSharingRecordDto;
import com.example.authservice.dto.ProfitSharingRecordDto;
import com.example.authservice.dto.UpdateProfitSharingStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface ProfitSharingRecordService {
    ProfitSharingRecordDto createProfitSharingRecord(CreateProfitSharingRecordDto recordDto, UserDetails currentUser);
    ProfitSharingRecordDto getProfitSharingRecordById(Long id);
    Page<ProfitSharingRecordDto> getAllProfitSharingRecords(Pageable pageable);
    Page<ProfitSharingRecordDto> getProfitSharingRecordsByOrderId(Long orderId, Pageable pageable);
    Page<ProfitSharingRecordDto> getProfitSharingRecordsByChannelId(Long channelId, Pageable pageable);
    Page<ProfitSharingRecordDto> getProfitSharingRecordsByMerchantId(Long merchantId, Pageable pageable);
    ProfitSharingRecordDto updateProfitSharingStatus(Long id, UpdateProfitSharingStatusDto statusDto, UserDetails currentUser);
}
