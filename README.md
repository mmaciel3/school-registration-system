# School registration system

This is a school registration system.

## Requirements
These are the requirements for this project:

> Design and implement simple school registration system
> - Assuming you already have a list of students
> - Assuming you already have a list of courses
> - A student can register to multiple courses
> - A course can have multiple students enrolled in it.
> - A course has 50 students maximum
> - A student can register to 5 course maximum
>
> Provide the following APIs:
> - Create students CRUD
> - Create courses CRUD
> - Create API for students to register to courses
> - Create Report API for admin user to view all relationships between students and course
>  + Filter all students with a specific course
>  +  Filter all courses for a specific student
>  + Filter all courses without any students
>  + Filter all students without any courses

## Tech stack
This application is build with:
* Java 16 with Spring Boot 2
* Maven
* Docker
* MySQL

## Setting up the project

### Pre-requirements
* Install [Docker](https://docs.docker.com/get-docker/)

### Database config
* Make a copy of file `.env.example` and rename it to `.env`.
* Enter values for your desired database user and password.
* Example of what the file should look like:
```
MYSQL_USER=myuser
MYSQL_PASSWORD=password123
```

### Running the application
After configuring the database, run `docker compose up`.

### Stopping the application
`docker compose down`

## API documentation
Start the application and navigate to http://localhost:8080/swagger-ui/.

This will show the API documentation with sample requests and responses.

## Testing
To execute the unit tests, run `mvn test`.