package com.exercise.school.database.repository;

import com.exercise.school.database.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findOneByEmailAddress(String emailAddress);

	@Query("SELECT s FROM Student s WHERE s.enrolledCourses IS EMPTY")
	Page<Student> findStudentsWithNoCourses(Pageable pageable);
}