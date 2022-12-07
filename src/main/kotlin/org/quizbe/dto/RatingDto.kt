package org.quizbe.dto

import javax.validation.constraints.*

class RatingDto {
    var id: Long? = null

    @field:Size(min = 2, max = 500, message = "{play.user.rating.comment.min.max}")
    var comment:  String? = null

    @field:NotNull(message = "{play.user.rating.value}")
    @field:Min(value = 1, message = "{play.user.rating.value}")
    @field:Max(value = 5, message = "{play.user.rating.value}")
    var value:Int? = null

    var outDated = false

    constructor() {}

    /**
     * Constructor
     * @param id
     * @param comment
     * @param value
     * @param outDated
     */
    constructor(id: Long?, comment: String?, value: Int?, outDated: Boolean) {
        this.id = id
        this.comment = comment
        this.value = value
        this.outDated = outDated
    }


    override fun toString(): String {
        return "RatingDto{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", value=" + value +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RatingDto

        if (id != other.id) return false
        if (comment != other.comment) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (value ?: 0)
        return result
    }
}