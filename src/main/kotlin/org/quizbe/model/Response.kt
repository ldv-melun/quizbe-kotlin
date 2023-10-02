package org.quizbe.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "response")
class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Basic
    @Column(name = "proposition", nullable = false)
    var proposition: String? = null

    @Basic
    @Column(name = "feedback", nullable = false)
    var feedback: String? = null

    // VALUE is a reserved word in h2, so VALUEQ (or force quoted column name...)
    @Basic
    @Column(name = "valueq", nullable = false)
    var value: Int? = null

    @ManyToOne
    var question: Question? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val response = o as Response
        return id === response.id && proposition == response.proposition
    }

    override fun hashCode(): Int {
        return Objects.hash(id, proposition)
    }

    override fun toString(): String {
        return "Response{" +
                "id=" + id +
                ", proposition='" + proposition + '\'' +
                ", feedback='" + feedback + '\'' +
                ", value=" + value +
                '}'
    }
}
