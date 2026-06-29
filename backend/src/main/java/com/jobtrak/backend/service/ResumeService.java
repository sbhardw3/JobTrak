package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.ResumeRequest;
import com.jobtrak.backend.dto.ResumeResponse;
import com.jobtrak.backend.entity.Resume;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResumeService {

	private final ResumeRepository resumeRepository;
	private final UserLookupService userLookupService;

	public ResumeService(ResumeRepository resumeRepository, UserLookupService userLookupService) {
		this.resumeRepository = resumeRepository;
		this.userLookupService = userLookupService;
	}

	public ResumeResponse create(String email, ResumeRequest request) {
		User user = userLookupService.getByEmail(email);
		Resume resume = new Resume(user, request.title().trim(), request.content().trim());
		return toResponse(resumeRepository.save(resume));
	}

	public List<ResumeResponse> getAll(String email) {
		User user = userLookupService.getByEmail(email);
		return resumeRepository.findByUserOrderByUpdatedAtDesc(user)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	public ResumeResponse getById(String email, Long id) {
		return toResponse(findOwnedResume(email, id));
	}

	public ResumeResponse update(String email, Long id, ResumeRequest request) {
		Resume resume = findOwnedResume(email, id);
		resume.update(request.title().trim(), request.content().trim());
		return toResponse(resumeRepository.save(resume));
	}

	public void delete(String email, Long id) {
		resumeRepository.delete(findOwnedResume(email, id));
	}

	private Resume findOwnedResume(String email, Long id) {
		User user = userLookupService.getByEmail(email);
		return resumeRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
	}

	private ResumeResponse toResponse(Resume resume) {
		return new ResumeResponse(
				resume.getId(),
				resume.getTitle(),
				resume.getContent(),
				resume.getCreatedAt(),
				resume.getUpdatedAt()
		);
	}
}
