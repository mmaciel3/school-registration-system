package com.exercise.school.controller;

import com.exercise.school.database.model.Course;
import com.exercise.school.database.model.Student;
import com.exercise.school.database.repository.CourseRepository;
import com.exercise.school.database.repository.StudentRepository;
import com.exercise.school.dto.CourseDto;
import com.exercise.school.dto.CoursePagedResponse;
import com.exercise.school.dto.EnrollmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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

	@GetMapping("")
	@Operation(summary = "List courses")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Courses listed",
					content = {@Content(schema = @Schema(implementation = CoursePagedResponse.class))}
			)
	})
	public ResponseEntity<Object> getCourses(
			@Parameter(description = "Get only courses with no students enrolled")
			@RequestParam(value = "noStudentsOnly", required = false, defaultValue = "false")
					boolean noStudentsOnly,
			@Parameter(description = "Page number")
			@RequestParam(value = "page", required = false, defaultValue = "0")
					int pageNumber,
			@Parameter(description = "Page size")
			@RequestParam(value = "size", required = false, defaultValue = "10")
					int pageSize
	) {
		return serviceHandler.processService((responseBuilder) -> {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<Course> response = noStudentsOnly ?
					this.courseRepository.findCoursesWithNoStudents(pageable) :
					this.courseRepository.findAll(pageable);
			responseBuilder.responseBody(new CoursePagedResponse(response));
		});
	}

	@PostMapping("")
	@Operation(summary = "Register course")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201", description = "Course registered",
					content = {@Content(schema = @Schema(implementation = Course.class))}
			)
	})
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> registerCourse(
			@Parameter(name = "Course registration request", required = true) @RequestBody CourseDto course) {
		return serviceHandler.processService((responseBuilder) -> {
			Course courseModel = course.toModel();
			this.courseRepository.save(courseModel);
			responseBuilder.responseBody(courseModel).statusCode(HttpStatus.CREATED);
		});
	}

	@GetMapping("/{id}")
	@Operation(summary = "Retrieve course")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Course retrieved",
					content = {@Content(schema = @Schema(implementation = Course.class))}
			),
			@ApiResponse(responseCode = "404", description = "Course not found")
	})
	public ResponseEntity<Object> getCourseById(
			@Parameter(description = "Course ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse(responseBuilder::responseBody, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@GetMapping("/{id}/students")
	@Operation(summary = "List enrolled students")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Enrolled students listed",
					content = {@Content(array = @ArraySchema(schema = @Schema(implementation = Student.class)))}
			),
			@ApiResponse(responseCode = "404", description = "Course not found")
	})
	public ResponseEntity<Object> getCourseStudents(
			@Parameter(description = "Course ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse(course -> responseBuilder.responseBody(course.getEnrolledStudents()),
						() -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update course")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Course updated",
					content = {@Content(schema = @Schema(implementation = Course.class))}
			),
			@ApiResponse(responseCode = "404", description = "Course not found")
	})
	public ResponseEntity<Object> updateCourse(
			@Parameter(description = "Course ID", required = true)
			@PathVariable("id")
					Long id,
			@Parameter(description = "course", required = true)
			@RequestBody
					CourseDto course) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse((courseFromDb) -> {
					courseFromDb.setName(course.getName());
					this.courseRepository.save(courseFromDb);
					responseBuilder.responseBody(courseFromDb);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete course")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Course deleted"),
			@ApiResponse(responseCode = "404", description = "Course not found")
	})
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Object> deleteCourse(
			@Parameter(description = "Course ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.courseRepository.findById(id)
				.ifPresentOrElse((courseFromDb) -> {
					this.courseRepository.deleteById(id);
					responseBuilder.statusCode(HttpStatus.NO_CONTENT);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PostMapping("/{id}/enroll")
	@Operation(summary = "Enroll in course")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successful enrollment"),
			@ApiResponse(responseCode = "400", description = "The course is full or the student has exceeded maximum enrolled courses"),
			@ApiResponse(responseCode = "404", description = "Course not found")
	})
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> enrollInCourse(
			@Parameter(description = "Course ID", required = true)
			@PathVariable("id")
					Long courseId,
			@Parameter(description = "Enrollment request", required = true)
			@RequestBody
					EnrollmentRequest enrollmentRequest
	) {

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
										responseBuilder.statusCode(HttpStatus.CREATED);
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