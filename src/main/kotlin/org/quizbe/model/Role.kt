package org.quizbe.model

import javax.persistence.*

@Entity
@Table(name = "ROLE")
class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    var name: String? = null

    constructor() {}
    constructor(name: String?) : super() {
        this.name = name
    }

    override fun toString(): String {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}'
    }
}