package com.exercise.school.controller;

import com.exercise.school.dto.CourseDto;
import com.exercise.school.dto.EnrollmentRequest;
import com.exercise.school.dto.PagedResponse;
import com.exercise.school.database.model.Course;
import com.exercise.school.database.repository.CourseRepository;
import com.exercise.school.database.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {
	private static final int MAX_COURSES_PER_STUDENT = 5;
	private static final int MAX_STUDENTS_PER_COURSE = 50;

	@Autowired
	private ServiceHandler serviceHandler;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private StudentRepository studentRepository;

	@PostMapping("")
	public ResponseEntity<Object> registerCourse(@RequestBody CourseDto course) {
		return serviceHandler.processService((responseBuilder) -> {
			Course courseModel = course.toModel();
			this.courseRepository.save(courseModel);
			responseBuilder.responseBody(courseModel).statusCode(HttpStatus.OK);
		});
	}

	@GetMapping("")
	public ResponseEntity<Object> getCourses(
			@RequestParam(value = "noStudentsOnly", required = false, defaultValue = "false") boolean noStudentsOnly,
			@RequestParam(value = "page", required = false, defaultValue = "0") int pageNumber,
			@RequestParam(value = "size", required = false, defaultValue = "10") int pageSize) {

		return serviceHandler.processService((responseBuilder) -> {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<Course> response = noStudentsOnly ?
					this.courseRepository.findCoursesWithNoStudents(pageable) :
					this.courseRepository.findAll(pageable);
			responseBuilder.responseBody(new PagedResponse<>(response));
		});
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getCourseById(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse(responseBuilder::responseBody, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@GetMapping("/{id}/students")
	public ResponseEntity<Object> getCourseStudents(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse(course -> responseBuilder.responseBody(course.getEnrolledStudents()),
						() -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Object> updateCourse(@PathVariable("id") Long id, @RequestBody CourseDto course) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse((courseFromDb) -> {
					courseFromDb.setName(course.getName());
					this.courseRepository.save(courseFromDb);
					responseBuilder.responseBody(courseFromDb);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteCourse(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse((courseFromDb) -> {
					this.courseRepository.deleteById(id);
					responseBuilder.statusCode(HttpStatus.NO_CONTENT);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PostMapping("/{id}/enroll")
	public ResponseEntity<Object> enrollInCourse(@PathVariable("id") Long courseId,
	                                             @RequestBody EnrollmentRequest enrollmentRequest) {

		Long studentId = enrollmentRequest.studentId();

		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(courseId)
				.ifPresentOrElse((course) -> this.studentRepository.findById(studentId).ifPresentOrElse(
								(student) -> {
									if (student.getEnrolledCourses().size() >= MAX_COURSES_PER_STUDENT) {
										final Map<String, String> responseBody = Map.of("message",
												"Student has exceeded maximum allowed courses");
										responseBuilder.responseBody(responseBody).statusCode(HttpStatus.BAD_REQUEST);
									} else if (course.getEnrolledStudents().size() >= MAX_STUDENTS_PER_COURSE) {
										final Map<String, String> responseBody = Map.of("message",
												"The course is full");
										responseBuilder.responseBody(responseBody).statusCode(HttpStatus.BAD_REQUEST);
									} else {
										course.getEnrolledStudents().add(student);
										this.courseRepository.save(course);
										responseBuilder.statusCode(HttpStatus.OK);
									}
								},
								() -> {
									final Map<String, String> responseBody = Map.of("message", "No student found with ID", "studentId", studentId.toString());
									responseBuilder.responseBody(responseBody).statusCode(HttpStatus.NOT_FOUND);
								}),
						() -> {
							final Map<String, String> responseBody = Map.of(
									"message",
									"No course found with ID",
									"courseId",
									courseId.toString()
							);
							responseBuilder.responseBody(responseBody).statusCode(HttpStatus.NOT_FOUND);
						}));
	}
}