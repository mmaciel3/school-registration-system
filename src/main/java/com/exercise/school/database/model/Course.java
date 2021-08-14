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
@Table(name = "course")
@Data
@EqualsAndHashCode(exclude = "enrolledStudents")
@JsonIgnoreProperties({"enrolledStudents"})
public class Course {
	@Id
	@GeneratedValue
	long id;

	@Column(name = "name", nullable = false)
	String name;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "course_enrollment",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "student_id")
	)
	Set<Student> enrolledStudents = new HashSet<>();
}