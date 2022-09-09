package org.quizbe.model

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity //  @DynamicInsert
// see https://thorben-janssen.com/dynamic-inserts-and-updates-with-spring-data-jpa/
// https://stackoverflow.com/questions/21721818/why-does-not-hibernate-set-dynamicinsert-by-default
// Question may be updated frequently (Response also, to test before :)
@Table(name = "QUESTION")
class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    var id: Long = 0

    @Basic
    @Column(name = "NAME", nullable = false, length = 50)
    lateinit var name: String

    @Lob
    @Column(name = "SENTENCE", nullable = false)
    lateinit var sentence: String

    @Basic
    @Column(nullable = false)
    var dateUpdate: LocalDateTime? = null

    /// trace de la personne conceptrice et co-conceptrice
    @Basic
    @Column(name = "DESIGNER", nullable = false, length = 50)
    lateinit var designer: String

    @Basic
    @Column(name = "CODESIGNER", nullable = false, length = 50)
    var codesigner: String = ""

    @Basic
    @Column(nullable = false)
    var visible = true

    ///
    @ManyToOne
    lateinit var scope: Scope

    @ManyToOne
    lateinit var topic: Topic

    @OneToMany(mappedBy = "question", orphanRemoval = true, cascade = [CascadeType.ALL])
    var ratings = mutableListOf<Rating>()

    @OneToMany(mappedBy = "question", orphanRemoval = true, cascade = [CascadeType.ALL])
    var responses = mutableListOf<Response>()

    /**
     * Get number of expected good responses
     * @return number of responses which are value > 0
     */
    val expectedGoodChoices: Int
        get() = responses.stream().filter { response: Response -> response.value!! >= 0 }.count().toInt()

    /**
     * get average ratings
     * @return average user ratings of this question
     */
    val avgRatings: Int
        get() = if (ratings.size == 0) 0 else ratings.stream().mapToInt { i: Rating -> i.value!! }.sum() / ratings.size


    fun removeResponses() {
        val it = responses.iterator()
        while (it.hasNext()) {
            val r = it.next()
            r.question = null
            it.remove()
        }
    }

    fun addResponse(response: Response) {
        if (!responses.contains(response)) {
            responses.add(response)
            response.question = this
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val scope = other as Question
        return id == scope.id && name == scope.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }

    override fun toString(): String {
        return "Question{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sentence='" + sentence + '\'' +
                ", datecrea=" + dateUpdate +
                ", designer='" + designer + '\'' +
                ", codesigner='" + codesigner + '\'' +
                ", visible='" + visible + '\'' +
                '}'
    }

}