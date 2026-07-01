package com.jobtrak.backend.repository;

import com.jobtrak.backend.entity.AiAnalysis;
import com.jobtrak.backend.entity.JobApplication;
import com.jobtrak.backend.entity.Resume;
import com.jobtrak.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

	List<AiAnalysis> findByUserOrderByCreatedAtDesc(User user);

	Optional<AiAnalysis> findByIdAndUser(Long id, User user);

	List<AiAnalysis> findByResume(Resume resume);

	List<AiAnalysis> findByJobApplication(JobApplication jobApplication);
}
