package com.exercise.school.dto;

import com.exercise.school.database.model.Student;
import org.springframework.data.domain.Page;

public class StudentPagedResponse extends PagedResponse<Student> {
	public StudentPagedResponse(Page<Student> queryResult) {
		super(queryResult);
	}
}