package com.yebin.sideproject.domain.track.controller;

import com.yebin.sideproject.domain.track.dto.TrackSummaryResponseDto;
import com.yebin.sideproject.domain.track.dto.TrackUploadRequestDto;
import com.yebin.sideproject.domain.track.dto.TrackUploadResponseDto;
import com.yebin.sideproject.domain.track.service.TrackService;
import com.yebin.sideproject.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "음원", description = "음원 관련 API")
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackService trackService;

    @PostMapping
    @Operation(summary = "음원등록", description = "토큰과 음원정보를 보내 음원 등록을 요청합니다.")
    public ResponseEntity<TrackUploadResponseDto> uploadTrack(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 헤더로 보낸 사용자 토큰 -> 사용자 검증
            @Valid @RequestBody TrackUploadRequestDto request
    ) {
        TrackUploadResponseDto data = trackService.requestUpload(userDetails.getUser(), request);
        return ResponseEntity.ok(data);
    }

    @GetMapping
    @Operation(summary = "내 음원 목록 조회", description = "로그인한 크리에이터 본인이 업로드한 음원 목록을 최신순으로 조회합니다.")
    public ResponseEntity<List<TrackSummaryResponseDto>> getMyTracks(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TrackSummaryResponseDto> data = trackService.getMyTracks(userDetails.getUser());
        return ResponseEntity.ok(data);
    }

}
