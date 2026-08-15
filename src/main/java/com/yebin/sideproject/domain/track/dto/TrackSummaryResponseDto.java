package com.yebin.sideproject.domain.track.dto;

import com.yebin.sideproject.domain.track.entity.enums.TrackStatus;

import java.time.LocalDateTime;

public record TrackSummaryResponseDto(
        Long trackId,
        String thumbnailUrl,
        String title,
        TrackStatus status,
        LocalDateTime createdAt
) {
}
