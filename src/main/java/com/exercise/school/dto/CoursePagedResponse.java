package com.exercise.school.dto;

import com.exercise.school.database.model.Course;
import org.springframework.data.domain.Page;

public class CoursePagedResponse extends PagedResponse<Course> {
	public CoursePagedResponse(Page<Course> queryResult) {
		super(queryResult);
	}
}