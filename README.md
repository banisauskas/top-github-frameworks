# Top GitHub Frameworks

## GitHub results

Links that return the same desired results, but in either HTML or API/JSON format.
- https://github.com/topics/framework?l=java&o=desc&s=stars
- https://github.com/search?q=topic:framework&type=Repositories&l=Java&o=desc&s=stars
- https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10

## Eclipse

Optional step when Eclipse is used:

1. `gradlew cleanEclipse eclipse`
1. Import project to Eclipse as an "Existing Project".

## Build and run without Docker

Requirements: JDK.

- `gradlew bootRun`

or

1. `gradlew clean build`
1. `java -jar build/libs/top-github-frameworks-1.0.jar`

Example URL `http://localhost:8080/top`

## Build and run with Docker locally

Requirements: JDK, Docker.

1. `gradlew clean build docker`
1. `docker run -p 8080:8080 banisauskas/top-github-frameworks`

Example URL `http://192.168.99.100:8080/top`

To shut down:

1. Ctrl+C
1. `docker ps` - note container ID, e.g., 81c723d22865.
1. `docker stop 81c723d22865`
1. `docker rm 81c723d22865`

## Build and run using Docker Hub (recommended)

Requirements: Docker.

- `docker run -p 8080:8080 banisauskas/top-github-frameworks`

Example URL `http://192.168.99.100:8080/top`

## API `top` endpoint

Retrieves 10 most active (as defined by the number of stars) GitHub Java frameworks.

- `GET /top` - sorted by the number of stars in descending order.
- `GET /top?orderByContribs=asc` - sorted by the number of contributors in ascending order.
- `GET /top?orderByContribs=desc` - sorted by the number of contributors in descending order.

Optionally accepts `Authorization` header (e.g., `Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=`).

An example call, which can be made using browser when authentication unnecessary: http://localhost:8080/top?orderByContribs=desc

## API `star` endpoint

Stars or unstars 1 specified GitHub repository.

- `PUT /star/{owner}/{repo}`
- `DELETE /star/{owner}/{repo}`

Requires `Authorization` header.

An example to star Spring Boot repository: `PUT http://localhost:8080/star/spring-projects/spring-boot`

## References

- https://developer.github.com/v3/search/
- https://developer.github.com/v3/repos/
- https://developer.github.com/v3/activity/starring/
- https://github.com/FasterXML/jackson-annotations
- https://www.baeldung.com/jackson-nested-values
- https://www.baeldung.com/jackson-ignore-null-fields
- https://spring.io/guides/gs/spring-boot-docker/
- https://hub.docker.com/r/banisauskas/top-github-frameworks/