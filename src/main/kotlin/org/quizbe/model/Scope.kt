package org.quizbe.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SCOPE")
class Scope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    var id: Long? = null

    @Basic
    @Column(name = "NAME", nullable = false, length = 50)
    lateinit var name: String

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var topic: Topic

    constructor() {}
    constructor(name: String, topic: Topic) {
        this.name = name
        this.topic = topic
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val scope = other as Scope
        return id === scope.id && name == scope.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }

    override fun toString(): String {
        return ("Scope{"
                + "id=" + id
                + ", name='" + name + '\''
                + '}')
    }
}