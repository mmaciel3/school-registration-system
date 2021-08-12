
package com.exercise.school.database.repository;

import com.exercise.school.database.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {

	@Query("SELECT c FROM Course c WHERE c.enrolledStudents IS EMPTY")
	Page<Course> findCoursesWithNoStudents(Pageable pageable);
}