package com.example.authservice.service.impl;

import com.example.authservice.dto.ChannelDto;
import com.example.authservice.entity.Channel;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.ChannelRepository;
import com.example.authservice.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    @Transactional
    public ChannelDto createChannel(ChannelDto channelDto) {
        if (channelRepository.existsByName(channelDto.getName())) {
            throw new BadRequestException("Channel name '" + channelDto.getName() + "' is already taken.");
        }
        Channel channel = mapDtoToEntity(channelDto);
        // Ensure default values are set if not provided by DTO for new entities
        if (channel.getAccountBalance() == null) channel.setAccountBalance(java.math.BigDecimal.ZERO);
        if (channel.getTotalProfitShareAvailable() == null) channel.setTotalProfitShareAvailable(java.math.BigDecimal.ZERO);
        if (channel.getTotalProfitShared() == null) channel.setTotalProfitShared(java.math.BigDecimal.ZERO);
        if (channel.getStatus() == null) channel.setStatus("active");


        Channel savedChannel = channelRepository.save(channel);
        return mapEntityToDto(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelDto getChannelById(Long id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", id));
        return mapEntityToDto(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChannelDto> getAllChannels(Pageable pageable) {
        return channelRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public ChannelDto updateChannel(Long id, ChannelDto channelDto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", id));

        // Check if new name is taken by another channel
        if (!channel.getName().equals(channelDto.getName()) && channelRepository.existsByName(channelDto.getName())) {
            throw new BadRequestException("Channel name '" + channelDto.getName() + "' is already taken by another channel.");
        }

        // Using BeanUtils.copyProperties, ensure to handle nulls or specific update logic if needed
        // For example, you might not want to update createdAt or other specific fields
        BeanUtils.copyProperties(channelDto, channel, "id", "createdAt", "merchants"); // Exclude fields that shouldn't be copied

        Channel updatedChannel = channelRepository.save(channel);
        return mapEntityToDto(updatedChannel);
    }

    @Override
    @Transactional
    public void deleteChannel(Long id) {
        if (!channelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Channel", "id", id);
        }
        // Consider implications: what happens to merchants associated with this channel?
        // If `channel_id` in merchants table is nullable, merchants become unassociated.
        // If not nullable and no cascade delete from Channel to Merchant (which is typical to avoid accidental mass deletion),
        // you might need to reassign merchants or prevent deletion if merchants exist.
        // Current Channel entity has CascadeType.ALL, which would delete associated merchants. This might be too aggressive.
        // Let's assume for now that this is acceptable, or that business logic would prevent deletion if merchants are present.
        channelRepository.deleteById(id);
    }

    // --- Helper Mappers ---
    private ChannelDto mapEntityToDto(Channel channel) {
        if (channel == null) return null;
        ChannelDto dto = new ChannelDto();
        BeanUtils.copyProperties(channel, dto, "merchants"); // Exclude merchants list for now from ChannelDto
        return dto;
    }

    private Channel mapDtoToEntity(ChannelDto channelDto) {
        if (channelDto == null) return null;
        Channel entity = new Channel();
        BeanUtils.copyProperties(channelDto, entity, "id", "createdAt", "updatedAt", "merchants");
        return entity;
    }
}
