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

import java.util.ArrayList;
import java.util.HashSet;
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
public class CourseApiTest {
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
	class given_registerCourse {

		@Test
		public void should_registerCourseAndReturnIt() throws Exception {
			JSONObject parameters = new JSONObject();
			parameters.put("name", "Course");

			given()
					.body(parameters.toString())
					.post("/courses")
					.then()
					.assertThat()
					.statusCode(201)
					.body("id", not(blankOrNullString()))
					.body("name", equalTo("Course"));

			List<Course> courses = courseRepository.findAll();
			assertThat(courses, hasSize(1));
		}
	}

	@Nested
	class given_listCourses {
		@Nested
		class when_noCoursesAreRegistered {
			@Test
			public void should_returnEmptyList() {
				List<Student> content = given().get("/courses")
						.then()
						.assertThat()
						.statusCode(200)
						.extract()
						.path("content");

				assertThat(content, Matchers.hasSize(0));
			}
		}

		@Nested
		class when_coursesAreRegistered {
			@BeforeEach
			public void register() {
				Course course1 = new Course();
				course1.setName("Chemistry");
				courseRepository.save(course1);

				Course course2 = new Course();
				course2.setName("Math");
				courseRepository.save(course2);

				Student student = new Student();
				student.setFirstName("First");
				student.setLastName("Last");
				student.setEmailAddress("Email");
				student.setEnrolledCourses(Set.of(course1));
				studentRepository.save(student);
			}

			@Nested
			class andNoStudentsOnlyParameterIsSupplied {
				@Test
				public void should_returnOnlyStudentsWithNoCourses() {
					List<Map<String, Object>> content = given().get("/courses?noStudentsOnly=true")
							.then()
							.assertThat()
							.statusCode(200)
							.extract()
							.path("content");

					assertThat(content, Matchers.hasSize(1));

					Map<String, Object> course = content.get(0);
					assertThat(course.get("name"), equalTo("Math"));
				}
			}

			@Nested
			class andNoStudentsOnlyParameterIsNotSupplied {
				@Test
				public void should_returnAllStudents() {
					List<Map<String, Object>> content = given().get("/courses?noStudentsOnly=false")
							.then()
							.assertThat()
							.statusCode(200)
							.extract()
							.path("content");

					assertThat(content, Matchers.hasSize(2));

					Map<String, Object> course1 = content.get(0);
					assertThat(course1.get("name"), equalTo("Chemistry"));

					Map<String, Object> course2 = content.get(1);
					assertThat(course2.get("name"), equalTo("Math"));
				}
			}
		}
	}

	@Nested
	class given_getById {

		@Nested
		class when_courseExists {
			@Test
			public void should_returnTheCourse() throws Exception {
				JSONObject parameters = new JSONObject();
				parameters.put("name", "Math");

				Integer id = given()
						.body(parameters.toString())
						.post("/courses")
						.then()
						.extract()
						.path("id");

				Course course = given()
						.get("/courses/" + id)
						.then()
						.statusCode(200)
						.extract()
						.as(Course.class);

				assertThat(course.getName(), equalTo("Math"));
			}
		}

		@Nested
		class when_courseDoesNotExist {
			@Test
			public void should_return404() {
				given()
						.get("/courses/901132212")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_listEnrolledStudents {
		@Nested
		class when_courseExists {
			@Test
			public void should_returnTheStudents() {
				Course course = new Course();
				course.setName("Course");
				courseRepository.save(course);

				Student student = new Student();
				student.setFirstName("First");
				student.setLastName("Last");
				student.setEmailAddress("Email");
				student.setEnrolledCourses(Set.of(course));
				studentRepository.save(student);

				long id = course.getId();

				List<Student> students = List.of(
						given()
								.get("/courses/" + id + "/students")
								.then()
								.statusCode(200)
								.extract()
								.as(Student[].class)
				);

				assertThat(students.size(), equalTo(1));
				assertThat(students.get(0).getFirstName(), equalTo("First"));
				assertThat(students.get(0).getLastName(), equalTo("Last"));
				assertThat(students.get(0).getEmailAddress(), equalTo("Email"));
			}
		}

		@Nested
		class when_courseDoesNotExist {
			@Test
			public void should_return404() {
				given()
						.get("/courses/901132212/students")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_updateCourse {

		@Nested
		class when_courseExists {

			@Test
			public void should_updateTheCourseAndReturnTheUpdatedData() throws Exception {
				Course course = new Course();
				course.setName("Mathe");
				courseRepository.save(course);

				long id = course.getId();

				JSONObject request = new JSONObject();
				request.put("name", "Math");

				String updatedName = given()
						.body(request.toString())
						.put("/courses/" + id)
						.then()
						.statusCode(200)
						.extract()
						.path("name");

				assertThat(updatedName, equalTo("Math"));
			}
		}

		@Nested
		class when_courseDoesNotExist {
			@Test
			public void should_return404() throws Exception {
				JSONObject request = new JSONObject();
				request.put("name", "Math");

				given()
						.body(request.toString())
						.put("/courses/901132212")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_deleteCourse {
		@Nested
		class when_courseExists {
			@Test
			public void should_deleteTheCourse() {
				Course course = new Course();
				course.setName("Math");
				courseRepository.save(course);

				long id = course.getId();

				given()
						.delete("/courses/" + id)
						.then()
						.statusCode(204);
			}
		}

		@Nested
		class when_courseDoesNotExist {
			@Test
			public void should_return404() {

				given()
						.delete("/courses/901132212")
						.then()
						.statusCode(404);
			}
		}
	}

	@Nested
	class given_enrollInCourse {
		@Nested
		class when_courseDoesNotExist {

			@Test
			public void should_return404() throws Exception {
				JSONObject request = new JSONObject();
				request.put("studentId", 100);

				given()
						.body(request.toString())
						.post("/courses/901132212/enroll")
						.then()
						.statusCode(404);
			}
		}

		@Nested
		class when_studentDoesNotExist {

			@Test
			public void should_return404() throws Exception {
				Course course = new Course();
				course.setName("Math");
				courseRepository.save(course);
				long id = course.getId();

				JSONObject request = new JSONObject();
				request.put("studentId", 100);

				given()
						.body(request.toString())
						.post("/courses/" + id + "/enroll")
						.then()
						.statusCode(404);
			}
		}

		@Nested
		class when_courseAndStudentExists {
			@Nested
			class and_studentHasReachedMaximumEnrolledCourses {

				@Test
				public void should_return400() throws Exception {
					List<Course> courses = new ArrayList<>();
					for (int i = 0; i < 6; i++) {
						Course course = new Course();
						course.setName("Course " + i);
						courseRepository.save(course);
						courses.add(course);
					}

					Student student = new Student();
					student.setFirstName("First");
					student.setLastName("Last");
					student.setEmailAddress("Email");
					student.setEnrolledCourses(new HashSet<>(courses.subList(0, 5)));
					studentRepository.save(student);

					JSONObject request = new JSONObject();
					request.put("studentId", student.getId());

					given()
							.body(request.toString())
							.post("/courses/" + courses.get(5).getId() + "/enroll")
							.then()
							.statusCode(400)
							.body("message", equalTo("Student has exceeded maximum allowed courses"));
				}
			}

			@Nested
			class and_courseHasReachedCapacity {

				@Test
				public void should_return400() throws Exception {
					Course course = new Course();
					course.setName("Course");
					courseRepository.save(course);

					List<Student> students = new ArrayList<>();
					for (int i = 0; i < 51; i++) {
						Student student = new Student();
						student.setFirstName("First " + i);
						student.setLastName("Last " + i);
						student.setEmailAddress("Email " + i);

						if (i < 50) {
							student.setEnrolledCourses(Set.of(course));
						}

						studentRepository.save(student);
						students.add(student);
					}

					JSONObject request = new JSONObject();
					request.put("studentId", students.get(50).getId());

					given()
							.body(request.toString())
							.post("/courses/" + course.getId() + "/enroll")
							.then()
							.statusCode(400)
							.body("message", equalTo("The course is full"));
				}
			}

			@Nested
			class and_bothStudentAndCourseHaveCapacity {
				@Test
				public void should_enrollUserInCourseAndReturn200() throws Exception {
					Course course = new Course();
					course.setName("Course");
					courseRepository.save(course);

					Student student = new Student();
					student.setFirstName("First");
					student.setLastName("Last");
					student.setEmailAddress("Email");
					studentRepository.save(student);

					JSONObject request = new JSONObject();
					request.put("studentId", student.getId());

					given()
							.body(request.toString())
							.post("/courses/" + course.getId() + "/enroll")
							.then()
							.statusCode(201);
				}
			}
		}
	}
}