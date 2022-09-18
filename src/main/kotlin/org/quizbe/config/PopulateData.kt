package org.quizbe.config

import org.quizbe.dto.*
import org.quizbe.exception.TopicNotFoundException
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.Rating
import org.quizbe.model.Role
import org.quizbe.model.User
import org.quizbe.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.sql.SQLIntegrityConstraintViolationException
import java.time.LocalDateTime

@Order(value = 1)
@Component
class PopulateData : ApplicationRunner {
    var logger = LoggerFactory.getLogger(PopulateData::class.java)

    private var admin: User? = null
    private var teacher: User? = null
    private var eleve: User? = null

    @Autowired
    var userService: UserService? = null

    @Autowired
    var roleService: RoleService? = null

    @Autowired
    var topicService: TopicService? = null

    @Autowired
    var questionService: QuestionService? = null

    @Autowired
    var ratingService: RatingService? = null


    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        val role = roleService!!.findByName("USER")
        if (role != null) return
        roleService!!.saveRole(Role("USER"))
        roleService!!.saveRole(Role("TEACHER"))
        roleService!!.saveRole(Role("ADMIN"))
        createUsers()
        addTopics()
        addQuestions()
        addRatings()
    }

    private fun addRatings() {
        val question1 = questionService!!.findById(1)
        var rating = Rating(null, "La vie est belle", 5, LocalDateTime.now(), question1, teacher)
        ratingService!!.save(rating)
        rating = Rating(null, "La vie, cette inconnue", 3, LocalDateTime.now(), question1, eleve)
        ratingService!!.save(rating)
    }

    private fun addTopics() {
        var topicDto = TopicDto("Topic1")

        topicDto.creatorUsername = teacher!!.username
        val scopeDtos: MutableList<ScopeDto> = ArrayList()
        scopeDtos.add(ScopeDto("scope1"))
        scopeDtos.add(ScopeDto("scope2"))
        topicDto.setScopesDtos(scopeDtos)
        topicService!!.saveTopicFromTopicDto(topicDto, null)
        topicDto = TopicDto()
        topicDto.name = "Topic2"
        topicDto.creatorUsername = teacher!!.username
        scopeDtos.add(ScopeDto("scope3"))
        topicDto.setScopesDtos(scopeDtos)
        topicService!!.saveTopicFromTopicDto(topicDto, null)

        // users subscribe to topic1
        val topic = topicService!!.findTopicById(1L).get()
//        admin!!.subscribedTopics.add(topic)
        topic.addSubscribedr(admin!!)
        topic.addSubscribedr(teacher!!)
        topic.addSubscribedr(eleve!!)

        topicService!!.save(topic)

    }

    private fun addQuestions() {
        val topic = topicService!!.getTopicById(1L).get()

        val responseDtos = mutableListOf<ResponseDto>()
        responseDtos.add(ResponseDto(null, "_42_", "feedback proposition 1", 1))
        responseDtos.add(ResponseDto(null, "proposition 2", "feedback proposition 2", -1))
        responseDtos.add(ResponseDto(null, "proposition 3", "feedback proposition 3", -2))
        val questionDto = QuestionDto(null, topic, 1L, admin!!.username)
        questionDto.name = "Question1"
        questionDto.sentence = "Answer to the Ultimate Question of Life, the Universe, and Everything"
        questionDto.responseDtos = responseDtos
        questionService!!.saveQuestionFromQuestionDto(questionDto)
    }

    @Throws(SQLIntegrityConstraintViolationException::class)
    private fun createUsers() {
        val userAdmin = userService!!.findById(1).orElse(null)
        if (userAdmin == null) {
            val roles: MutableSet<String> = HashSet()
            roles.add("USER")
            roles.add("TEACHER")
            roles.add("ADMIN")
            val adminDto = UserDto("admin", "admin@admin.org", "adminadmin", roles)
            logger.info("userAdmin : $adminDto")
            admin = userService!!.saveUserFromUserDto(adminDto)
            roles.remove("ADMIN")
            val teacherDto = UserDto("prof", "prof@prof.org", "profprof", roles)
            logger.info("userTeacher : $teacherDto")
            teacher = userService!!.saveUserFromUserDto(teacherDto)

            roles.remove("TEACHER")
            val eleveDto = UserDto("eleve", "eleve@eleve.org", "eleveeleve", roles)
            logger.info("userEleve : $eleveDto")
            eleve = userService!!.saveUserFromUserDto(eleveDto)
        }
        this.admin = userService!!.findByUsername("admin") ?: throw UserNotFoundException("admin not found")
        this.teacher = userService!!.findByUsername("prof") ?: throw UserNotFoundException("prof not found")
        this.eleve = userService!!.findByUsername("eleve") ?: throw UserNotFoundException("eleve not found")
    }
}