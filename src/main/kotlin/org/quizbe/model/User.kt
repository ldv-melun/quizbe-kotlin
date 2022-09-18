package org.quizbe.model

import org.quizbe.config.QuizbeGlobals
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors
import javax.persistence.*

@Entity
@Table(name = "USERQ")
class User {
    @Transient
    var logger : Logger = LoggerFactory.getLogger(User::class.java)

//    @Transient
//    private val VALID_HOURS_DEFAULT_PW = QuizbeGlobals.pwLifeTimeHours

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "USERNAME", unique = true, nullable = false)
    lateinit var username: String

    @Column(name = "EMAIL", unique = true, nullable = false)
    lateinit var email: String

    @Column(name = "PASSWORD", nullable = false)
    lateinit var password: String

    @Column(name = "ACTIVE")
    var isEnabled: Boolean = true

    @Basic
    var dateUpdatePassword: LocalDateTime? = null

    @Basic
    var dateDefaultPassword: LocalDateTime? = null

    @Basic
    var defaultPlainTextPassword: String? = null

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.MERGE])
    @JoinTable(name = "USER_ROLES", joinColumns = [JoinColumn(name = "USER_ID", referencedColumnName = "id")], inverseJoinColumns = [JoinColumn(name = "ROLE_ID", referencedColumnName = "id")])
    var roles = mutableSetOf<Role>()

//    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "subscribers", cascade = [CascadeType.ALL])
    @ManyToMany(mappedBy = "subscribers", fetch = FetchType.EAGER)
    var subscribedTopics = mutableSetOf<Topic>()

    @OneToMany(mappedBy = "creator", cascade = [CascadeType.ALL])
    var topics = mutableListOf<Topic>()

    constructor() {

    }

    constructor(userName: String, email: String, password: String, roles: MutableSet<Role>) : super() {
        this.username = userName
        this.email = email
        this.password = password
        this.roles = roles
        isEnabled = true
    }

    val subscribedTopicsVisibles: List<Topic>
        get() = subscribedTopics.stream().filter { topic: Topic -> topic.isVisible }.collect(Collectors.toList())

    fun isSubscribed(topic: Topic): Boolean {
        return subscribedTopics.contains(topic)
    }


    val visibleTopics: List<Topic>
        get() = topics.stream().filter { topic: Topic -> topic.isVisible }.collect(Collectors.toList())

    override fun toString(): String {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + isEnabled +
                '}'
    }

    fun mustChangePassword(): Boolean {
        return null == dateUpdatePassword
    }

    fun hoursWaitingToChangePassword(maxHoursTimeLimite : Long): Long {
        if (dateUpdatePassword != null) return 0
        val rest = ChronoUnit.HOURS.between(
            LocalDateTime.now(),
            dateDefaultPassword!!.plusHours(maxHoursTimeLimite)
        )
//        logger.info("this.getSetDateDefaultPassword()" + dateDefaultPassword)
        return rest
    }

    fun canWrite(question: Question): Boolean {
        return question.designer.equals(username, ignoreCase = true)
    }

    fun canWrite(topic: Topic): Boolean {
        return topic.creator?.id === id
    }

    fun hasDefaultPlainTextPasswordInvalidate(maxHoursTimeLimite : Long): Boolean {
//        logger.info("TEMPS EN HEURE LIMITE VALIDATION : maxHoursTimeLimite")
        return (this.mustChangePassword()
                &&
                (this.dateDefaultPassword == null
                        ||
                        this.dateDefaultPassword!!.plusHours(maxHoursTimeLimite).isBefore(LocalDateTime.now())))
    }


    companion object {
        @JvmStatic
        fun generateRandomPassword(len: Int): String {
            val chars = "0123456789ABCDEFGHIJKLMNOPQRTUVWXYZabcdefghijkmnopqrstuvwxyz"
            val rnd = Random()
            val sb = StringBuilder(len)
            for (i in 0 until len) {
                sb.append(chars[rnd.nextInt(chars.length)])
            }
            return sb.toString()
        }
    }

}