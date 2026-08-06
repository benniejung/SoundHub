package com.yebin.sideproject.domain.track.dto;

public record TrackUploadResponseDto(
        String trackId,
        String audioUploadUrl,
        String thumbnailUploadUrl,
        Integer expiresInSec
) {
}
