package com.exercise.school.api;

import com.exercise.school.SchoolApplication;
import com.exercise.school.database.model.Course;
import com.exercise.school.database.model.Student;
import com.exercise.school.database.repository.CourseRepository;
import com.exercise.school.database.repository.StudentRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
		classes = SchoolApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class StudentApiTest {

	@LocalServerPort
	private int port;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private CourseRepository courseRepository;

	@BeforeEach
	public void setupAndCleanDatabase() {
		RestAssured.port = port;
		RestAssured.requestSpecification = new RequestSpecBuilder()
				.setContentType(ContentType.JSON)
				.setAccept(ContentType.JSON)
				.build();

		studentRepository.deleteAll();
		courseRepository.deleteAll();
	}

	@Nested
	class given_registerStudent {
		@Nested
		class when_emailAddress {
			@Nested
			class isNotYetRegistered {
				@Test
				public void should_registerStudentAndReturnIt() throws Exception {
					JSONObject parameters = new JSONObject();
					parameters.put("firstName", "John");
					parameters.put("lastName", "Doe");
					parameters.put("emailAddress", "john.doe@mail.com");

					given()
							.body(parameters.toString())
							.post("/students")
							.then()
							.assertThat()
							.statusCode(201)
							.body("id", not(blankOrNullString()))
							.body("firstName", equalTo("John"))
							.body("lastName", equalTo("Doe"))
							.body("emailAddress", equalTo("john.doe@mail.com"));

					List<Student> students = studentRepository.findAll();
					assertThat(students, hasSize(1));
				}
			}

			@Nested
			class hasAlreadyBeenRegistered {
				@Test
				public void should_return400() throws Exception {
					Student student = new Student();
					student.setEmailAddress("john.doe@mail.com");
					student.setFirstName("Johnny");
					student.setLastName("Doe");
					studentRepository.save(student);

					JSONObject parameters = new JSONObject();
					parameters.put("firstName", "John");
					parameters.put("lastName", "Doe");
					parameters.put("emailAddress", "john.doe@mail.com");

					given()
							.body(parameters.toString())
							.post("/students")
							.then()
							.assertThat()
							.statusCode(400)
							.body("message", equalTo("A student with this email has already been registered"));

					List<Student> students = studentRepository.findAll();
					assertThat(students, hasSize(1));
				}
			}
		}
	}

	@Nested
	class given_listStudents {
		@Nested
		class when_noStudentsAreRegistered {
			@Test
			public void should_returnEmptyListOfStudents() {
				List<Student> content = given().get("/students")
						.then()
						.assertThat()
						.statusCode(200)
						.extract()
						.path("content");

				assertThat(content, Matchers.hasSize(0));
			}
		}

		@Nested
		class when_studentsAreRegistered {
			@BeforeEach
			public void register() {
				Course course = new Course();
				course.setName("Chemistry");
				courseRepository.save(course);

				Student student1 = new Student();
				student1.setFirstName("First1");
				student1.setLastName("Last1");
				student1.setEmailAddress("Email1");
				student1.setEnrolledCourses(Set.of(course));
				studentRepository.save(student1);

				Student student2 = new Student();
				student2.setFirstName("First2");
				student2.setLastName("Last2");
				student2.setEmailAddress("Email2");

				studentRepository.save(student2);
			}

			@Nested
			class andNoCoursesOnlyParameterIsSupplied {
				@Test
				public void should_returnOnlyStudentsWithNoCourses() {
					List<Map<String, Object>> content = given().get("/students?noCoursesOnly=true")
							.then()
							.assertThat()
							.statusCode(200)
							.extract()
							.path("content");

					assertThat(content, Matchers.hasSize(1));

					Map<String, Object> student = content.get(0);
					assertThat(student.get("firstName"), equalTo("First2"));
					assertThat(student.get("lastName"), equalTo("Last2"));
					assertThat(student.get("emailAddress"), equalTo("Email2"));
				}
			}

			@Nested
			class andNoCoursesOnlyParameterIsNotSupplied {
				@Test
				public void should_returnAllStudents() {
					List<Map<String, Object>> content = given().get("/students?noCoursesOnly=false")
							.then()
							.assertThat()
							.statusCode(200)
							.extract()
							.path("content");

					assertThat(content, Matchers.hasSize(2));

					Map<String, Object> student1 = content.get(0);
					assertThat(student1.get("firstName"), equalTo("First1"));
					assertThat(student1.get("lastName"), equalTo("Last1"));
					assertThat(student1.get("emailAddress"), equalTo("Email1"));

					Map<String, Object> student2 = content.get(1);
					assertThat(student2.get("firstName"), equalTo("First2"));
					assertThat(student2.get("lastName"), equalTo("Last2"));
					assertThat(student2.get("emailAddress"), equalTo("Email2"));

				}
			}
		}
	}

	@Nested
	class given_getById {

		@Nested
		class when_studentExists {
			@Test
			public void should_returnTheStudent() throws Exception {
				JSONObject parameters = new JSONObject();
				parameters.put("firstName", "John");
				parameters.put("lastName", "Doe");
				parameters.put("emailAddress", "john.doe@mail.com");

				Integer id = given()
						.body(parameters.toString())
						.post("/students")
						.then()
						.extract()
						.path("id");

				Student student = given()
						.get("/students/" + id)
						.then()
						.statusCode(200)
						.extract()
						.as(Student.class);

				assertThat(student.getFirstName(), equalTo("John"));
				assertThat(student.getLastName(), equalTo("Doe"));
				assertThat(student.getEmailAddress(), equalTo("john.doe@mail.com"));
			}
		}

		@Nested
		class when_studentDoesNotExist {
			@Test
			public void should_return404() {
				given()
						.get("/students/901132212")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_getStudentCourses {
		@Nested
		class when_studentExists {
			@Test
			public void should_returnTheStudent() {
				Course course = new Course();
				course.setName("Course");
				courseRepository.save(course);

				Student student = new Student();
				student.setFirstName("First");
				student.setLastName("Last");
				student.setEmailAddress("Email");
				student.setEnrolledCourses(Set.of(course));
				studentRepository.save(student);

				long id = student.getId();

				List<Course> courses = List.of(
						given()
								.get("/students/" + id + "/courses")
								.then()
								.statusCode(200)
								.extract()
								.as(Course[].class)
				);

				assertThat(courses.size(), equalTo(1));
				assertThat(courses.get(0).getName(), equalTo("Course"));
			}
		}

		@Nested
		class when_studentDoesNotExist {
			@Test
			public void should_return404() {
				given()
						.get("/students/901132212/courses")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_updateStudent {
		@Nested
		class when_studentExists {
			@Test
			public void should_updateTheStudentAndReturnTheUpdatedData() throws Exception {
				Student student = new Student();
				student.setFirstName("First");
				student.setLastName("Last");
				student.setEmailAddress("Email");
				studentRepository.save(student);

				long id = student.getId();

				JSONObject request = new JSONObject();
				request.put("firstName", "NewName");
				request.put("lastName", "Last");
				request.put("emailAddress", "Email");

				String updatedName = given()
						.body(request.toString())
						.put("/students/" + id)
						.then()
						.statusCode(200)
						.extract()
						.path("firstName");

				assertThat(updatedName, equalTo("NewName"));
			}
		}

		@Nested
		class when_studentDoesNotExist {
			@Test
			public void should_return404() throws Exception {
				JSONObject request = new JSONObject();
				request.put("firstName", "NewName");
				request.put("lastName", "Last");
				request.put("emailAddress", "Email");

				given()
						.body(request.toString())
						.put("/students/901132212")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_deleteStudent {
		@Nested
		class when_studentExists {
			@Test
			public void should_deleteTheStudent() {
				Student student = new Student();
				student.setFirstName("First");
				student.setLastName("Last");
				student.setEmailAddress("Email");
				studentRepository.save(student);

				long id = student.getId();

				given()
						.delete("/students/" + id)
						.then()
						.statusCode(204);
			}
		}

		@Nested
		class when_studentDoesNotExist {
			@Test
			public void should_return404() {
				given()
						.delete("/students/901132212")
						.then()
						.statusCode(404);
			}
		}
	}
}