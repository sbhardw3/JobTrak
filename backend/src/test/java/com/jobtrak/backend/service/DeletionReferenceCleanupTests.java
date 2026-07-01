package com.jobtrak.backend.service;

import com.jobtrak.backend.entity.AiAnalysis;
import com.jobtrak.backend.entity.ApplicationStatus;
import com.jobtrak.backend.entity.JobApplication;
import com.jobtrak.backend.entity.Resume;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.AiAnalysisRepository;
import com.jobtrak.backend.repository.JobApplicationRepository;
import com.jobtrak.backend.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletionReferenceCleanupTests {

	@Mock
	private JobApplicationRepository jobApplicationRepository;

	@Mock
	private ResumeRepository resumeRepository;

	@Mock
	private AiAnalysisRepository aiAnalysisRepository;

	@Mock
	private UserLookupService userLookupService;

	@Test
	void deleteApplicationDetachesExistingAiAnalysesBeforeDeleting() {
		String email = "qa@example.com";
		User user = new User("QA User", email, "hash");
		JobApplication application = new JobApplication(
				user,
				"NinerMine",
				"Software Engineer",
				"Build Spring Boot APIs",
				null,
				null,
				ApplicationStatus.APPLIED
		);
		AiAnalysis analysis = analysisFor(user, null, application);

		when(userLookupService.getByEmail(email)).thenReturn(user);
		when(jobApplicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(application));
		when(aiAnalysisRepository.findByJobApplication(application)).thenReturn(List.of(analysis));

		new JobApplicationService(jobApplicationRepository, aiAnalysisRepository, userLookupService)
				.delete(email, 1L);

		assertThat(analysis.getJobApplication()).isNull();
		verify(aiAnalysisRepository).saveAll(List.of(analysis));
		verify(jobApplicationRepository).delete(application);
	}

	@Test
	void deleteResumeDetachesExistingAiAnalysesBeforeDeleting() {
		String email = "qa@example.com";
		User user = new User("QA User", email, "hash");
		Resume resume = new Resume(user, "QA Resume", "Java Spring Boot resume");
		AiAnalysis analysis = analysisFor(user, resume, null);

		when(userLookupService.getByEmail(email)).thenReturn(user);
		when(resumeRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(resume));
		when(aiAnalysisRepository.findByResume(resume)).thenReturn(List.of(analysis));

		new ResumeService(resumeRepository, aiAnalysisRepository, userLookupService)
				.delete(email, 1L);

		assertThat(analysis.getResume()).isNull();
		verify(aiAnalysisRepository).saveAll(List.of(analysis));
		verify(resumeRepository).delete(resume);
	}

	private AiAnalysis analysisFor(User user, Resume resume, JobApplication application) {
		return new AiAnalysis(
				user,
				resume,
				application,
				"resume snapshot",
				"job description snapshot",
				80,
				"[]",
				"[]",
				"[]",
				"[]",
				"[]",
				"[]",
				"cover letter",
				"GEMINI",
				"gemini-2.5-flash"
		);
	}
}
