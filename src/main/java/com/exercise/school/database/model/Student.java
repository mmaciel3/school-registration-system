package com.exercise.school.database.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
@Data
@EqualsAndHashCode(exclude = "enrolledCourses")
@JsonIgnoreProperties("enrolledCourses")
public class Student {
	@Id
	@GeneratedValue
	long id;

	@Column(name = "first_name", nullable = false)
	String firstName;

	@Column(name = "last_name", nullable = false)
	String lastName;

	@Column(name = "email_address", nullable = false)
	String emailAddress;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "course_enrollment",
			joinColumns = @JoinColumn(name = "student_id"),
			inverseJoinColumns = @JoinColumn(name = "course_id")
	)
	Set<Course> enrolledCourses = new HashSet<>();
}