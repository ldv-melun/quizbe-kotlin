package org.quizbe.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class TopicDto {
    var id: Long? = null

    @field:NotBlank(message = "{topic.name.blank}")
    @field:Size(min = 3, max = 30, message = "{topic.name.min.max}")
    var name:  String? = null

    @field:NotNull
    @field:Size(min = 1)
    private var scopesDtos = mutableListOf<ScopeDto>()
    var isVisible = true

    // auto set = current user (username)
    var creatorUsername: String? = null

    constructor()

    constructor(name: String?) {
        this.name = name
    }

    fun getScopesDtos(): MutableList<ScopeDto> {
        return scopesDtos
    }

    fun setScopesDtos(scopesDtos: MutableList<ScopeDto>?) {
        if (scopesDtos != null) {
            this.scopesDtos = scopesDtos
        } else {
            this.scopesDtos.clear()
        }
    }

    override fun toString(): String {
        return "TopicDto{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", visible=" + isVisible +
                ", scopes=" + scopesDtos +
                ", creatorUsername='" + creatorUsername + '\'' +
                '}'
    }
}