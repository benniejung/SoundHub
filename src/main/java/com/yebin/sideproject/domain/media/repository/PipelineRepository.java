package com.yebin.sideproject.domain.media.repository;

import com.yebin.sideproject.domain.media.entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
}
