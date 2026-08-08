package com.yebin.sideproject.domain.track.controller;

import com.yebin.sideproject.domain.track.dto.S3EventNotificationDto;
import com.yebin.sideproject.domain.track.service.TrackService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/s3-events")
public class S3EventWebhookController {
    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody S3EventNotificationDto dto) {
        for (S3EventNotificationDto.Record record : dto.records()) {
            trackService.handleS3UploadComplete(record.s3().object());
        }
        return ResponseEntity.ok().build(); // 전달하는 바디 없이 성공만 알릴때
    }
}
