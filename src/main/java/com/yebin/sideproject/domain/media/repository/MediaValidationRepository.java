package com.yebin.sideproject.domain.media.repository;

import com.yebin.sideproject.domain.media.entity.ReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaValidationRepository extends JpaRepository<ReviewDecision, Long> {
}
