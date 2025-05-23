package com.example.authservice.service.impl;

import com.example.authservice.dto.CreateProfitSharingRecordDto;
import com.example.authservice.dto.ProfitSharingRecordDto;
import com.example.authservice.dto.UpdateProfitSharingStatusDto;
import com.example.authservice.entity.Channel;
import com.example.authservice.entity.Merchant;
import com.example.authservice.entity.Order;
import com.example.authservice.entity.ProfitSharingRecord;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.ChannelRepository;
import com.example.authservice.repository.MerchantRepository;
import com.example.authservice.repository.OrderRepository;
import com.example.authservice.repository.ProfitSharingRecordRepository;
import com.example.authservice.service.ProfitSharingRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class ProfitSharingRecordServiceImpl implements ProfitSharingRecordService {

    private final ProfitSharingRecordRepository profitSharingRecordRepository;
    private final OrderRepository orderRepository;
    private final ChannelRepository channelRepository;
    private final MerchantRepository merchantRepository;
    // private final UserRepository userRepository; // If currentUser needs to be mapped to User entity for some logic

    @Override
    @Transactional
    public ProfitSharingRecordDto createProfitSharingRecord(CreateProfitSharingRecordDto recordDto, UserDetails currentUser) {
        Order order = orderRepository.findById(recordDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", recordDto.getOrderId()));
        Channel channel = channelRepository.findById(recordDto.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", recordDto.getChannelId()));

        ProfitSharingRecord record = new ProfitSharingRecord();
        record.setOrder(order);
        record.setChannel(channel);

        if (recordDto.getMerchantId() != null) {
            Merchant merchant = merchantRepository.findById(recordDto.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", recordDto.getMerchantId()));
            record.setMerchant(merchant);
        }

        record.setAmount(recordDto.getAmount());
        record.setCurrency(StringUtils.hasText(recordDto.getCurrency()) ? recordDto.getCurrency() : "CNY");
        record.setStatus(StringUtils.hasText(recordDto.getStatus()) ? recordDto.getStatus() : "PENDING");
        record.setCalculationDetails(recordDto.getCalculationDetails());
        record.setCommissionRateSnapshot(recordDto.getCommissionRateSnapshot());
        record.setNotes(recordDto.getNotes());
        // paidAt will be set when status changes to PAID

        ProfitSharingRecord savedRecord = profitSharingRecordRepository.save(record);
        return mapEntityToDto(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitSharingRecordDto getProfitSharingRecordById(Long id) {
        ProfitSharingRecord record = profitSharingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfitSharingRecord", "id", id));
        return mapEntityToDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfitSharingRecordDto> getAllProfitSharingRecords(Pageable pageable) {
        return profitSharingRecordRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfitSharingRecordDto> getProfitSharingRecordsByOrderId(Long orderId, Pageable pageable) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        return profitSharingRecordRepository.findByOrderId(orderId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfitSharingRecordDto> getProfitSharingRecordsByChannelId(Long channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", "id", channelId);
        }
        return profitSharingRecordRepository.findByChannelId(channelId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfitSharingRecordDto> getProfitSharingRecordsByMerchantId(Long merchantId, Pageable pageable) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant", "id", merchantId);
        }
        return profitSharingRecordRepository.findByMerchantId(merchantId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public ProfitSharingRecordDto updateProfitSharingStatus(Long id, UpdateProfitSharingStatusDto statusDto, UserDetails currentUser) {
        ProfitSharingRecord record = profitSharingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfitSharingRecord", "id", id));

        // Authorization check (e.g., Admin only) handled by @PreAuthorize in controller
        record.setStatus(statusDto.getStatus());
        if ("PAID".equalsIgnoreCase(statusDto.getStatus())) {
            record.setPaidAt(statusDto.getPaidAt() != null ? statusDto.getPaidAt() : java.time.OffsetDateTime.now());
        } else if (statusDto.getPaidAt() != null && !"PAID".equalsIgnoreCase(statusDto.getStatus())) {
            // If paidAt is provided but status is not PAID, it's a bit ambiguous.
            // For simplicity, only set paidAt if status is PAID. Or clear paidAt if status is not PAID.
             record.setPaidAt(null); // Clear paidAt if status is not PAID
        }


        ProfitSharingRecord updatedRecord = profitSharingRecordRepository.save(record);
        return mapEntityToDto(updatedRecord);
    }

    // --- Helper Mapper ---
    private ProfitSharingRecordDto mapEntityToDto(ProfitSharingRecord record) {
        if (record == null) return null;
        ProfitSharingRecordDto dto = new ProfitSharingRecordDto();
        BeanUtils.copyProperties(record, dto, "order", "channel", "merchant");

        if (record.getOrder() != null) {
            dto.setOrderId(record.getOrder().getId());
            dto.setOrderNumber(record.getOrder().getOrderNumber());
        }
        if (record.getChannel() != null) {
            dto.setChannelId(record.getChannel().getId());
            dto.setChannelName(record.getChannel().getName());
        }
        if (record.getMerchant() != null) {
            dto.setMerchantId(record.getMerchant().getId());
            dto.setMerchantName(record.getMerchant().getName());
        }
        return dto;
    }
}
