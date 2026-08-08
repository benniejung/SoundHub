package com.yebin.sideproject.domain.track.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackUploadRequestDto(
        @NotBlank(message = "title은 필수 입력 사항입니다")
        String title,

        @NotNull
        String genre,

        String description,

        @NotBlank
        String audioFileName,

        @NotBlank
        String audioContentType, // File.type 으로 자동으로 보내짐

        @NotBlank
        String thumbnailFileName,

        @NotBlank
        String thumbnailContentType // File.type 으로 자동으로 보내짐
) {

}
