package com.yebin.sideproject.domain.track.entity;

import com.yebin.sideproject.domain.track.entity.enums.FileCategory;
import com.yebin.sideproject.domain.track.entity.enums.FileProcessStatus;
import com.yebin.sideproject.domain.track.entity.enums.FileType;
import com.yebin.sideproject.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "track_files")
@Getter
@NoArgsConstructor
public class TrackFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", length = 10)
    private FileCategory fileCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 30)
    private FileType fileType;

    @Column(length = 10)
    private String format;

    @Column(name = "bitrate")
    private Integer bitrate;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "image_size", length = 20)
    private String imageSize;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "file_hash", unique = true, length = 64)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_process_status", nullable = false, length = 20)
    private FileProcessStatus fileProcessStatus;

    @Builder
    public TrackFile(Track track, FileCategory fileCategory, FileType fileType, String format, Integer bitrate,
                      Integer durationSec, String imageSize, String storagePath) {
        this.track = track;
        this.fileCategory = fileCategory;
        this.fileType = fileType;
        this.format = format;
        this.bitrate = bitrate;
        this.durationSec = durationSec;
        this.imageSize = imageSize;
        this.storagePath = storagePath;
        this.fileProcessStatus = FileProcessStatus.PENDING;
    }

    public void assignFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
}
