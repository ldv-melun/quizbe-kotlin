package org.quizbe.dto

import org.quizbe.model.Topic
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class QuestionDto {
    var id: Long? = null
    var topic: Topic? = null
    var idScope: Long? = null
    var visible = false

    @field:NotBlank(message = "{question.name.blank}")
    @field:Size(min = 3, max = 30, message = "{question.name.min.max}")
    var name: String =""

    @field:NotBlank(message = "{question.sentence.blank}")
    @field:Size(min = 3, max = 800, message = "{question.sentence.min.max}")
    var sentence: String = ""

    @field:NotBlank(message = "{question.designer.blank}")
    @field:Size(min = 3, max = 30, message = "{question.designer.min.max}")
    var designer: String = ""

    var codesigners = ""

    @field:NotNull
    @field:Size(min = 2, max = 10)
    @Valid
    var responseDtos = mutableListOf<ResponseDto>()

    constructor() {}

    /**
     * QuestionDto
     * @param id id
     * @param topic topic
     * @param idScope
     * @param creatorUsername
     */
    constructor(id: Long?, topic: Topic, idScope: Long, creatorUsername: String) {
        this.id = id
        this.topic = topic
        this.idScope = idScope
        designer = creatorUsername
    }

    override fun toString(): String {
        return "QuestionDto(id=$id, topic=$topic, idScope=$idScope, visible=$visible, name=$name, sentence=$sentence," +
                " designer=$designer, codesigners='$codesigners', responseDtos=$responseDtos)"
    }


}