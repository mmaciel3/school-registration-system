package com.exercise.school.dto;

import com.exercise.school.database.model.Course;
import lombok.Data;

@Data
public class CourseDto {
	String name;

	public Course toModel() {
		Course course = new Course();
		course.setName(this.name);
		return course;
	}
}