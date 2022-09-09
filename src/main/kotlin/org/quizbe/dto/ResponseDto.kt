package org.quizbe.dto

import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class ResponseDto {
    var id: Long? = null

    @field:NotBlank(message = "{response.proposition.blank}")
    @field:Size(min = 3, max = 150, message = "{response.proposition.min.max}")
    var proposition:  String? = null

    @field:NotBlank(message = "{response.feedback.blank}")
    @field:Size(min = 10, max = 255, message = "{response.feedback.min.max}")
    var feedback:  String? = null

    @field:Min(value = -2, message = "{response.value.min}")
    @field:Max(value = 2, message = "{response.value.max}")
    var value: Int? = null

    constructor() {}

    /**
     * Constructor
     * @param id
     * @param proposition
     * @param feedback
     * @param value
     */
    constructor(id: Long?, proposition: String?, feedback: String?, value: Int?) {
        this.id = id
        this.proposition = proposition
        this.feedback = feedback
        this.value = value
    }

    override fun toString(): String {
        return "ResponseDto{" +
                "id=" + id +
                ", proposition='" + proposition + '\'' +
                ", feedback='" + feedback + '\'' +
                ", value=" + value +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ResponseDto
        return id == that.id && proposition == that.proposition
    }

    override fun hashCode(): Int {
        return Objects.hash(id, proposition)
    }
}