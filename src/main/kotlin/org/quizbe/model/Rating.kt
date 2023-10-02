package org.quizbe.model

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "rating", uniqueConstraints = [UniqueConstraint(columnNames = ["question_id", "user_id"])])
class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Lob
    @Column(nullable = false)
    var comment: String = ""

    @Basic
    @Column(name = "valuerating", nullable = false)
    var value: Int? = null

    @Basic
    @Column(nullable = false)
    var dateUpdate: LocalDateTime? = null

    @ManyToOne
    var question: Question? = null

    @ManyToOne
    var user: User? = null

    constructor(id: Long?, comment: String?, value: Int?, dateUpdate: LocalDateTime?, question: Question?, user: User?, isObsolete: Boolean = false) {
        this.id = id
        this.comment = comment ?: ""
        this.value = value
        this.dateUpdate = dateUpdate
        this.question = question
        this.user = user
        this.isObsolete = isObsolete
    }

    constructor() {}

    val isOutDated: Boolean
        get() = question!!.dateUpdate!!.isAfter(dateUpdate)

    var isObsolete: Boolean = false

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val rating = o as Rating
        return id === rating.id && comment == rating.comment && value == rating.value && dateUpdate == rating.dateUpdate
    }

    override fun hashCode(): Int {
        return Objects.hash(id, comment, value, dateUpdate)
    }

    override fun toString(): String {
        return "Rating{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", value=" + value +
                ", dateUpdate=" + dateUpdate +
                '}'
    }
}
