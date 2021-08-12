package com.exercise.school.controller;

import com.exercise.school.database.model.Student;
import com.exercise.school.database.repository.StudentRepository;
import com.exercise.school.dto.PagedResponse;
import com.exercise.school.dto.StudentDto;
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
@RequestMapping("/students")
public class StudentController {
	@Autowired
	private ServiceHandler serviceHandler;

	@Autowired
	private StudentRepository studentRepository;

	@PostMapping("")
	public ResponseEntity<Object> registerStudent(@RequestBody StudentDto student) {
		return serviceHandler.processService((responseBuilder) -> {
			final String emailAddress = student.getEmailAddress();
			this.studentRepository.findOneByEmailAddress(emailAddress)
					.ifPresentOrElse((duplicateStudent) -> {
						final Map<String, String> responseBody = Map.of("message", "A student with this email has already been registered", "emailAddress", emailAddress);
						responseBuilder.responseBody(responseBody).statusCode(HttpStatus.BAD_REQUEST);
					}, () -> {
						Student studentModel = student.toModel();
						this.studentRepository.save((studentModel));
						responseBuilder.responseBody(studentModel).statusCode(HttpStatus.OK);
					});
		});
	}

	@GetMapping("")
	public ResponseEntity<Object> getStudents(
			@RequestParam(value = "noCoursesOnly", required = false, defaultValue = "false") boolean noCoursesOnly,
			@RequestParam(value = "page", required = false, defaultValue = "0") int pageNumber,
			@RequestParam(value = "size", required = false, defaultValue = "10") int pageSize) {

		return serviceHandler.processService((responseBuilder) -> {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<Student> response = noCoursesOnly ?
					this.studentRepository.findStudentsWithNoCourses(pageable) :
					this.studentRepository.findAll(pageable);
			responseBuilder.responseBody(new PagedResponse<>(response));
		});
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getStudentById(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse(responseBuilder::responseBody, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@GetMapping("/{id}/courses")
	public ResponseEntity<Object> getStudentCourses(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse(student -> responseBuilder.responseBody(student.getEnrolledCourses()),
						() -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Object> updateStudent(@PathVariable("id") Long id, @RequestBody StudentDto student) {
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
	public ResponseEntity<Object> deleteStudent(@PathVariable("id") Long id) {
		return serviceHandler.processService((responseBuilder) -> this.studentRepository.findById(id)
				.ifPresentOrElse((studentFromDb) -> {
					this.studentRepository.deleteById(id);
					responseBuilder.statusCode(HttpStatus.NO_CONTENT);
				}, () -> responseBuilder.statusCode(HttpStatus.NOT_FOUND)));
	}
}