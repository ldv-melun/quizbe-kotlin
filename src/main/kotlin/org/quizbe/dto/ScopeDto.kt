package org.quizbe.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class ScopeDto(var id: Long? = null) {

    @field:NotBlank(message = "{scope.name.blank}")
    @field:Size(min = 3, max = 30, message = "{scope.name.min.max}")
    lateinit var name:  String

    constructor(name: String) : this(id = null){
        this.name = name
    }

    constructor(id: Long?, name: String) : this(name) {
        this.id = id
    }

    override fun toString(): String {
        return "ScopeDto{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}'
    }
}