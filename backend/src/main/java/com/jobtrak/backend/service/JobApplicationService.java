package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.JobApplicationRequest;
import com.jobtrak.backend.dto.JobApplicationResponse;
import com.jobtrak.backend.dto.StatusUpdateRequest;
import com.jobtrak.backend.entity.AiAnalysis;
import com.jobtrak.backend.entity.ApplicationStatus;
import com.jobtrak.backend.entity.JobApplication;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.AiAnalysisRepository;
import com.jobtrak.backend.repository.JobApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JobApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final AiAnalysisRepository aiAnalysisRepository;
	private final UserLookupService userLookupService;

	public JobApplicationService(
			JobApplicationRepository jobApplicationRepository,
			AiAnalysisRepository aiAnalysisRepository,
			UserLookupService userLookupService
	) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.aiAnalysisRepository = aiAnalysisRepository;
		this.userLookupService = userLookupService;
	}

	public JobApplicationResponse create(String email, JobApplicationRequest request) {
		User user = userLookupService.getByEmail(email);
		JobApplication application = new JobApplication(
				user,
				request.company().trim(),
				request.jobTitle().trim(),
				trimToNull(request.jobDescription()),
				trimToNull(request.jobUrl()),
				trimToNull(request.notes()),
				resolveStatus(request.status())
		);

		return toResponse(jobApplicationRepository.save(application));
	}

	public List<JobApplicationResponse> getAll(String email) {
		User user = userLookupService.getByEmail(email);
		return jobApplicationRepository.findByUserOrderByUpdatedAtDesc(user)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	public JobApplicationResponse getById(String email, Long id) {
		return toResponse(findOwnedApplication(email, id));
	}

	public JobApplicationResponse update(String email, Long id, JobApplicationRequest request) {
		JobApplication application = findOwnedApplication(email, id);
		application.update(
				request.company().trim(),
				request.jobTitle().trim(),
				trimToNull(request.jobDescription()),
				trimToNull(request.jobUrl()),
				trimToNull(request.notes()),
				resolveStatus(request.status())
		);

		return toResponse(jobApplicationRepository.save(application));
	}

	public JobApplicationResponse updateStatus(String email, Long id, StatusUpdateRequest request) {
		JobApplication application = findOwnedApplication(email, id);
		application.updateStatus(request.status());
		return toResponse(jobApplicationRepository.save(application));
	}

	@Transactional
	public void delete(String email, Long id) {
		JobApplication application = findOwnedApplication(email, id);
		List<AiAnalysis> analyses = aiAnalysisRepository.findByJobApplication(application);
		analyses.forEach(AiAnalysis::detachJobApplication);
		aiAnalysisRepository.saveAll(analyses);
		jobApplicationRepository.delete(application);
	}

	private JobApplication findOwnedApplication(String email, Long id) {
		User user = userLookupService.getByEmail(email);
		return jobApplicationRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
	}

	private ApplicationStatus resolveStatus(ApplicationStatus status) {
		return status == null ? ApplicationStatus.SAVED : status;
	}

	private String trimToNull(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private JobApplicationResponse toResponse(JobApplication application) {
		return new JobApplicationResponse(
				application.getId(),
				application.getCompany(),
				application.getJobTitle(),
				application.getJobDescription(),
				application.getJobUrl(),
				application.getNotes(),
				application.getStatus(),
				application.getCreatedAt(),
				application.getUpdatedAt()
		);
	}
}
