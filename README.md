# quizbe app

## Main concepts of tool

* Students themselves design (and co-design) quizzes (at home or in topic)
* Teacher gets interesting feedbacks of understanding.
* The teacher guides students in design quiz.
* Students evaluate and comment other quizzes (students and teachers!).
* Teacher reuses questions of students (and hers) for global evaluation (via export and through third-party solutions).

## Spring boot application (Spring Boot Kotlin MVC Web)

Education scope

Default users/pw : `admin/adminadmin`, `prof/profprof`, `eleve/eleveeleve`

@see private fun createUsers() in conf/PopulateData.kt

Default database : H2 in memory (can be change into application.properties)

Free Open Source Licence (TODO) 

Version executable (septembre 2023) : [quizbe-0.8.10.jar](./docs/quizbe-0.8.10.jar)

À placer dans un dossier et y placer une copie de `application.properties` pour paramétrer l'application.

Launch : `java -jar quizbe-0.8.10.jar --server.port=8080`

