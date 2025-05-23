package com.example.authservice.service.impl;

import com.example.authservice.dto.MerchantDto;
import com.example.authservice.entity.Channel;
import com.example.authservice.entity.Merchant;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.ChannelRepository;
import com.example.authservice.repository.MerchantRepository;
import com.example.authservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final ChannelRepository channelRepository; // To link merchant to a channel

    @Override
    @Transactional
    public MerchantDto createMerchant(MerchantDto merchantDto) {
        if (merchantRepository.existsByName(merchantDto.getName())) {
            throw new BadRequestException("Merchant name '" + merchantDto.getName() + "' is already taken.");
        }

        Merchant merchant = new Merchant();
        mapDtoToEntity(merchantDto, merchant); // Use a helper that handles channel linking

        if (merchantDto.getChannelId() != null) {
            Channel channel = channelRepository.findById(merchantDto.getChannelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", merchantDto.getChannelId()));
            merchant.setChannel(channel);
        }
        // Default status if not provided by DTO
        if (merchant.getStatus() == null) merchant.setStatus("pending");


        Merchant savedMerchant = merchantRepository.save(merchant);
        return mapEntityToDto(savedMerchant);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantDto getMerchantById(Long id) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", id));
        return mapEntityToDto(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantDto> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantDto> getMerchantsByChannelId(Long channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("Channel", "id", channelId);
        }
        return merchantRepository.findByChannelId(channelId, pageable).map(this::mapEntityToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MerchantDto> searchMerchantsByName(String name, Pageable pageable) {
        return merchantRepository.findByNameContainingIgnoreCase(name, pageable).map(this::mapEntityToDto);
    }


    @Override
    @Transactional
    public MerchantDto updateMerchant(Long id, MerchantDto merchantDto) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", id));

        // Check if new name is taken by another merchant
        if (merchantDto.getName() != null && !merchant.getName().equals(merchantDto.getName()) && merchantRepository.existsByName(merchantDto.getName())) {
            throw new BadRequestException("Merchant name '" + merchantDto.getName() + "' is already taken by another merchant.");
        }

        mapDtoToEntity(merchantDto, merchant); // Update fields

        if (merchantDto.getChannelId() != null) {
            if (merchant.getChannel() == null || !merchant.getChannel().getId().equals(merchantDto.getChannelId())) {
                Channel channel = channelRepository.findById(merchantDto.getChannelId())
                        .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", merchantDto.getChannelId()));
                merchant.setChannel(channel);
            }
        } else {
            merchant.setChannel(null); // Allow unsetting channel
        }


        Merchant updatedMerchant = merchantRepository.save(merchant);
        return mapEntityToDto(updatedMerchant);
    }

    @Override
    @Transactional
    public void deleteMerchant(Long id) {
        if (!merchantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Merchant", "id", id);
        }
        merchantRepository.deleteById(id);
    }

    // --- Helper Mappers ---
    private MerchantDto mapEntityToDto(Merchant merchant) {
        if (merchant == null) return null;
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto, "channel"); // Exclude channel object itself
        if (merchant.getChannel() != null) {
            dto.setChannelId(merchant.getChannel().getId());
            dto.setChannelName(merchant.getChannel().getName()); // Populate channelName
        }
        return dto;
    }

    private void mapDtoToEntity(MerchantDto merchantDto, Merchant merchant) {
        // Copy basic properties, excluding id, createdAt, updatedAt, channel (handled separately)
        BeanUtils.copyProperties(merchantDto, merchant, "id", "createdAt", "updatedAt", "channel", "channelId", "channelName");
        // channelId is used to fetch and set the Channel entity separately
    }
}
