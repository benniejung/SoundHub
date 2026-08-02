package com.yebin.sideproject.domain.media.controller;

import com.yebin.sideproject.domain.media.service.MediaValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media-validations")
@RequiredArgsConstructor
public class MediaValidationController {

    private final MediaValidationService mediaValidationService;
}
