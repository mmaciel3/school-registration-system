package com.exercise.school.dto;

import com.exercise.school.database.model.Student;
import lombok.Data;

@Data
public class StudentDto {
	String firstName;
	String lastName;
	String emailAddress;

	public Student toModel() {
		Student student = new Student();
		student.setFirstName(this.firstName);
		student.setLastName(this.lastName);
		student.setEmailAddress(this.emailAddress);
		return student;
	}
}