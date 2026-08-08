package com.yebin.sideproject.domain.track.dto;

// S3에서 저장됐다고 알릴때 보내는 정보들

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record S3EventNotificationDto (
        @JsonProperty("Records") List<Record> records
) {
    public record Record(
            String eventName,
            S3Info s3
    ) {}

    public record S3Info(
            Bucket bucket,
            S3Object object
    ) {}

    public record Bucket(String name) {}

    public record S3Object(
            String key,
            long size,
            String eTag,
            String contentType
    ) {}
}
