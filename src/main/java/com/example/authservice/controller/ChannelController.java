package com.example.authservice.controller;

import com.example.authservice.dto.ChannelDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.service.ChannelService;
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
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChannelDto> createChannel(@Valid @RequestBody ChannelDto channelDto) {
        ChannelDto createdChannel = channelService.createChannel(channelDto);
        return new ResponseEntity<>(createdChannel, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChannelDto> getChannelById(@PathVariable Long id) {
        ChannelDto channel = channelService.getChannelById(id);
        return ResponseEntity.ok(channel);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChannelDto>> getAllChannels(@PageableDefault(size = 20) Pageable pageable) {
        Page<ChannelDto> channels = channelService.getAllChannels(pageable);
        return ResponseEntity.ok(channels);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChannelDto> updateChannel(@PathVariable Long id, @Valid @RequestBody ChannelDto channelDto) {
        ChannelDto updatedChannel = channelService.updateChannel(id, channelDto);
        return ResponseEntity.ok(updatedChannel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Channel deleted successfully with id: " + id));
    }
}
