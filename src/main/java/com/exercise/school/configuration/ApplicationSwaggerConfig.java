package com.exercise.school.configuration;

import com.exercise.school.database.model.Course;
import com.exercise.school.database.model.Student;
import com.exercise.school.dto.CoursePagedResponse;
import com.exercise.school.dto.StudentPagedResponse;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;
import java.util.Set;

@Configuration
@EnableSwagger2
public class ApplicationSwaggerConfig {
	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = Set.of("application/json");

	@Bean
	public Docket docket(TypeResolver typeResolver) {
		final List<Response> globalResponses = List.of(
				new ResponseBuilder()
						.code("500")
						.description("Internal Error")
						.build());

		return new Docket(DocumentationType.SWAGGER_2)
				.additionalModels(
						typeResolver.resolve(Course.class),
						typeResolver.resolve(CoursePagedResponse.class),
						typeResolver.resolve(Student.class),
						typeResolver.resolve(StudentPagedResponse.class)
				)
				.globalResponses(HttpMethod.GET, globalResponses)
				.globalResponses(HttpMethod.POST, globalResponses)
				.globalResponses(HttpMethod.PUT, globalResponses)
				.globalResponses(HttpMethod.DELETE, globalResponses)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.exercise.school"))
				.paths(PathSelectors.any())
				.build()
				.produces(DEFAULT_PRODUCES_AND_CONSUMES)
				.consumes(DEFAULT_PRODUCES_AND_CONSUMES)
				.useDefaultResponseMessages(false);
	}
}