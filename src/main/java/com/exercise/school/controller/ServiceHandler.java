package com.exercise.school.controller;

import com.exercise.school.dto.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class ServiceHandler {
	public ResponseEntity<Object> processService(Consumer<HttpResponse.HttpResponseBuilder> consumer) {

		try {
			final HttpResponse.HttpResponseBuilder responseBuilder = HttpResponse.builder();
			consumer.accept(responseBuilder);
			return responseBuilder.build().toResponseEntity();
		} catch (Exception e) {
			Map<String, String> responseBody = Map.of("message", "Internal server error");
			return HttpResponse.builder()
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
					.responseBody(responseBody)
					.build()
					.toResponseEntity();
		}
	}
}