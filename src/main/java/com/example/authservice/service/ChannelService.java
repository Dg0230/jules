package com.example.authservice.service;

import com.example.authservice.dto.ChannelDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChannelService {
    ChannelDto createChannel(ChannelDto channelDto);
    ChannelDto getChannelById(Long id);
    Page<ChannelDto> getAllChannels(Pageable pageable);
    ChannelDto updateChannel(Long id, ChannelDto channelDto);
    void deleteChannel(Long id);
}
