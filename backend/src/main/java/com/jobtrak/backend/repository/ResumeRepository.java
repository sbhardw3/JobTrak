package com.jobtrak.backend.repository;

import com.jobtrak.backend.entity.Resume;
import com.jobtrak.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

	List<Resume> findByUserOrderByUpdatedAtDesc(User user);

	Optional<Resume> findByIdAndUser(Long id, User user);
}
