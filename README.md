# Top GitHub Frameworks

## —— Assignment ——

## Functional Requirements

Implement a service that connects to GitHub API and retrieves a list of 10 most active (as defined by the  number of stars) Java frameworks.

For each framework identified the following attributes must be exposed by your REST API:

- General info:
  - Name
  - Description
  - Licence name
  - Link to the repo
  - Whether or not current user (see below) has put a star on the repo
- Key metrics:
  - Number of contributors (including anonymous)
  - Number of stars

The API should make it possible to return the entries sorted by either key metric so it can answer a question like: "What are top 10 most active Java frameworks? Those with the least number of contributors should be listed first."

To give it some personal touch, the API should allow starring/un-starring a repo. Obviously, this requires authenticating against GitHub. The presence or absence of a personal star is included in the API response as described above.

To summarize, your REST API should expose endpoints to:

1. Query the most active frameworks, with or without one’s own GitHub credentials supplied (if they are supplied, the response includes whether or not the repo is starred by the user)
1. Star the repo
1. Un-star the repo

## Technical Requirements

1. The code is written in any JVM language (e.g. Java / Groovy / Scala / Clojure / Kotlin) or a reasonable mix of those
1. Your build scripts produce a Docker image which runs with minimal fuss:
  `$ docker run image-tag` or even `$ docker-compose up`

## Evaluation Criteria

- Convenience of building and running the application, making as little assumptions about OS, Maven/Gradle versions installed, etc. Ideally, JDK and Docker should suffice.
- Code is readable, tidy and is consistent in style and formatting
- Effort to minimize boilerplate code imposed by the language or framework of choice
- Code is well structured into packages
- Clear and concise README
- Reasonable use of version control and commit messages
- Functional requirements fulfilled and covered by tests
- Value of the test code and its focus on asserting functionality vs. implementation details
- [Soundness](https://en.oxforddictionaries.com/definition/soundness) of REST API design
- Effectiveness of retrieving information from GitHub API
- Simplicity of overall approach and lack of over-engineering

## —— Implementation ——

**GitHub pages**

Links that return the same desired results, but in either HTML or API/JSON format.
- https://github.com/topics/framework?l=java&o=desc&s=stars
- https://github.com/search?q=topic:framework&type=Repositories&l=Java&o=desc&s=stars
- https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10

**Eclipse**

Optional step if Eclipse is used:

1. `gradlew cleanEclipse eclipse`
1. Import project to Eclipse as an "Existing Project".

**Build and run without Docker**

Requirements: JDK.

- `gradlew bootRun`

or

1. `gradlew clean build`
1. `java -jar build/libs/top-github-frameworks-1.0.jar`

Example URL `http://localhost:8080/top`

**Build and run with Docker locally**

Requirements: JDK, Docker.

- `gradlew clean build docker`
- `docker run -p 8080:8080 banisauskas/top-github-frameworks`

Example URL `http://192.168.99.100:8080/top`

To shut down:

- Ctrl+C
- `docker ps` - note container ID, e.g., 81c723d22865.
- `docker stop 81c723d22865`
- `docker rm 81c723d22865`

**Build and run using Docker Hub (recommended)**

Requirements: Docker.

- `docker run -p 8080:8080 banisauskas/top-github-frameworks`

**References**

- https://developer.github.com/v3/search/
- https://developer.github.com/v3/repos/
- https://developer.github.com/v3/activity/starring/
- https://github.com/FasterXML/jackson-annotations
- https://www.baeldung.com/jackson-nested-values
- https://www.baeldung.com/jackson-ignore-null-fields
- https://spring.io/guides/gs/spring-boot-docker/
- https://hub.docker.com/r/banisauskas/top-github-frameworks/

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