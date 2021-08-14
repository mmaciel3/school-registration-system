package com.exercise.school.controller;

import com.exercise.school.database.model.Course;
import com.exercise.school.database.model.Student;
import com.exercise.school.database.repository.StudentRepository;
import com.exercise.school.dto.StudentDto;
import com.exercise.school.dto.StudentPagedResponse;
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
@RequestMapping("/students")
public class StudentController {
	@Autowired
	private ServiceHandler serviceHandler;

	@Autowired
	private StudentRepository studentRepository;

	@PostMapping("")
	@Operation(summary = "Register student")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201", description = "Student registered",
					content = {@Content(schema = @Schema(implementation = Student.class))}
			),
			@ApiResponse(responseCode = "400", description = "A student already exists with the given email address"),
	})
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> registerStudent(
			@Parameter(name = "Student registration request", required = true) @RequestBody StudentDto student) {
		return serviceHandler.processService((responseBuilder) -> {
			final String emailAddress = student.getEmailAddress();
			this.studentRepository.findOneByEmailAddress(emailAddress)
					.ifPresentOrElse((duplicateStudent) -> {
						final Map<String, String> responseBody = Map.of("message", "A student with this email has already been registered", "emailAddress", emailAddress);
						responseBuilder.responseBody(responseBody).statusCode(HttpStatus.BAD_REQUEST);
					}, () -> {
						Student studentModel = student.toModel();
						this.studentRepository.save((studentModel));
						responseBuilder.responseBody(studentModel).statusCode(HttpStatus.CREATED);
					});
		});
	}

	@GetMapping("")
	@Operation(summary = "List students")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Students listed",
					content = {@Content(schema = @Schema(implementation = StudentPagedResponse.class))}
			)
	})
	public ResponseEntity<Object> getStudents(
			@Parameter(description = "Get only students enrolled in no courses")
			@RequestParam(value = "noCoursesOnly", required = false, defaultValue = "false")
					boolean noCoursesOnly,
			@Parameter(description = "Page number")
			@RequestParam(value = "page", required = false, defaultValue = "0")
					int pageNumber,
			@Parameter(description = "Page size")
			@RequestParam(value = "size", required = false, defaultValue = "10")
					int pageSize) {

		return serviceHandler.processService((responseBuilder) -> {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<Student> response = noCoursesOnly ?
					this.studentRepository.findStudentsWithNoCourses(pageable) :
					this.studentRepository.findAll(pageable);
			responseBuilder.responseBody(new StudentPagedResponse(response));
		});
	}

	@GetMapping("/{id}")
	@Operation(summary = "Retrieve student")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Student retrieved",
					content = {@Content(schema = @Schema(implementation = Student.class))}
			),
			@ApiResponse(responseCode = "404", description = "Student not found")
	})
	public ResponseEntity<Object> getStudentById(
			@Parameter(description = "Student ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse(responseBuilder::responseBody, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@GetMapping("/{id}/courses")
	@Operation(summary = "List student courses")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Student courses listed",
					content = {@Content(array = @ArraySchema(schema = @Schema(implementation = Course.class)))}
			),
			@ApiResponse(responseCode = "404", description = "Student not found")
	})
	public ResponseEntity<Object> getStudentCourses(
			@Parameter(description = "Student ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse(student -> responseBuilder.responseBody(student.getEnrolledCourses()),
						() -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update student")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200", description = "Student updated",
					content = {@Content(schema = @Schema(implementation = Student.class))}
			),
			@ApiResponse(responseCode = "404", description = "Student not found")
	})
	public ResponseEntity<Object> updateStudent(
			@Parameter(description = "Student ID", required = true)
			@PathVariable("id")
					Long id,
			@Parameter(description = "student", required = true)
			@RequestBody
					StudentDto student
	) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse((studentFromDb) -> {
					studentFromDb.setFirstName(student.getFirstName());
					studentFromDb.setLastName(student.getLastName());
					studentFromDb.setEmailAddress(student.getEmailAddress());
					this.studentRepository.save(studentFromDb);
					responseBuilder.responseBody(student);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete student")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Student deleted"),
			@ApiResponse(responseCode = "404", description = "Student not found")
	})
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Object> deleteStudent(
			@Parameter(description = "Student ID", required = true)
			@PathVariable("id")
					Long id
	) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse((studentFromDb) -> {
					this.studentRepository.deleteById(id);
					responseBuilder.statusCode(HttpStatus.NO_CONTENT);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}
}