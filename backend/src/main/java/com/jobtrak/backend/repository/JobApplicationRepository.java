package com.jobtrak.backend.repository;

import com.jobtrak.backend.entity.JobApplication;
import com.jobtrak.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

	List<JobApplication> findByUserOrderByUpdatedAtDesc(User user);

	Optional<JobApplication> findByIdAndUser(Long id, User user);
}
