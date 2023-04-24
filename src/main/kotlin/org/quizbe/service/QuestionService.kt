package org.quizbe.service

import org.quizbe.dao.QuestionRepository
import org.quizbe.dao.ResponseRepository
import org.quizbe.dao.ScopeRepository
import org.quizbe.dto.QuestionDto
import org.quizbe.dto.ResponseDto
import org.quizbe.exception.QuestionNotFoundException
import org.quizbe.exception.ScopeNotFoundException
import org.quizbe.model.Question
import org.quizbe.model.Response
import org.quizbe.model.Scope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QuestionService @Autowired constructor(
    private val questionRepository: QuestionRepository,
    private val scopeRepository: ScopeRepository,
    private val responseRepository: ResponseRepository
) {
    var logger: Logger = LoggerFactory.getLogger(QuestionService::class.java)
    fun saveQuestionFromQuestionDto(questionDto: QuestionDto): Boolean {
        val question = convertToQuestion(questionDto)
        questionRepository.save(question)
        return true
    }

    private fun convertToQuestion(questionDto: QuestionDto): Question {
        val question: Question
        if (questionDto.id != null) {
            question =
                questionRepository.findById(questionDto.id!!.toLong()).orElseThrow { QuestionNotFoundException() }!!
            question.removeResponses()
        } else {
            question = Question()
        }
        val topic = questionDto.topic!!
        val scope: Scope = if (questionDto.idScope != null) {
            scopeRepository.findById(questionDto.idScope!!.toLong()).orElseThrow { ScopeNotFoundException() }!!
        } else {
            throw ScopeNotFoundException()
        }
        question.topic = topic
        question.scope = scope
        question.codesigner = questionDto.codesigners
        question.designer = questionDto.designer
        question.name = questionDto.name
        question.sentence = questionDto.sentence
        question.visible = questionDto.visible
        question.dateUpdate = LocalDateTime.now()
        // add/update all responses
        for (responseDto in questionDto.responseDtos) {
            val response = convertDtoToResponse(responseDto)
            question.addResponse(response)
        }
        return question
    }

    private fun convertDtoToResponse(responseDto: ResponseDto): Response {
        val response = Response()
        if (responseDto.id != null) {
            response.id = responseDto.id
        }
        response.proposition = responseDto.proposition
        response.feedback = responseDto.feedback
        response.value = responseDto.value
        return response
    }

    private fun convertResponseToDto(response: Response): ResponseDto {
        val responseDto = ResponseDto()
        responseDto.id = response.id
        responseDto.proposition = response.proposition
        responseDto.feedback = response.feedback
        responseDto.value = response.value
        return responseDto
    }

    fun findQuestionDtoById(id: Long): QuestionDto {
        val question = questionRepository.findById(id)
        return if (question.isPresent) {
            fromQuestionToQuestionDto(question.get())
        } else {
            throw QuestionNotFoundException("Invalid question Id:$id")
        }
    }

    private fun fromQuestionToQuestionDto(question: Question): QuestionDto {
        val questionDto = QuestionDto()
        questionDto.designer = question.designer
        questionDto.id = question.id
        questionDto.name = question.name
        questionDto.sentence = question.sentence
        questionDto.visible = question.visible
        questionDto.topic = question.topic
        questionDto.idScope = question.scope.id
        val responseDtos = mutableListOf<ResponseDto>()
        for (response in question.responses) {
            responseDtos.add(convertResponseToDto(response))
        }
        questionDto.responseDtos = responseDtos
        return questionDto
    }

    fun findById(id: Long): Question {
        return questionRepository.findById(id).orElseThrow { QuestionNotFoundException() }!!
    }

    fun findPreviousByIdQuestion(idQuestion: Long, scope: Scope): Question? {
        val idPrevious = questionRepository.findPreviousById(idQuestion, scope)
//        logger.info("id previous :$idPrevious")
        return if (idPrevious != null) findById(idPrevious.toLong()) else null
    }

    fun findFirstByScope(scope: Scope): Question? {
        val idFirst = questionRepository.findFirstByScope(scope)
//        logger.info("id previous :$idPrevious")
        return if (idFirst != null) findById(idFirst.toLong()) else null
    }

    fun findLastByScope(scope: Scope): Question? {
        val idLast = questionRepository.findLastByScope(scope)
//        logger.info("id previous :$idPrevious")
        return if (idLast != null) findById(idLast.toLong()) else null
    }

    fun findNextByIdQuestion(idQuestion: Long, scope: Scope): Question? {
        val idNext = questionRepository.findNextById(idQuestion, scope)
//        logger.info("id next :$idNext")
        return if (idNext != null) findById(idNext.toLong()) else null
    }

    fun delete(question: Question) {
        questionRepository.delete(question)
    }

    fun questionToTextRaw(question: Question?): String {
        val build = StringBuilder()
        if (question != null) {
            build.append(question.sentence).append(": ").append("\n\n")
            val responses = responseRepository.findByQuestion(question)
            if (responses != null) {
                for (response in responses) {
                    if (response != null) {
                        build.append("[ ] ").append(response.proposition).append("\n")
                    }
                }
            }
        }
        return build.toString()
    }


    /**
     * Fait une somme des values, la value de la question est divisée par la somme * 100, pour trouver la value en % sur 100 */


    /**
     * TODO Export question to Moodle
     *
     * @param question to XML Moodle transform
     * @return XML Moodle image of question
     */
    fun questionToXMLMoodle(question: Question): String {
        val build = StringBuilder()
        if (question != null) {
            build.append("<question type=\"multichoice\">")
            build.append("<name>")
            build.append("<text>")
            build.append(question.name)
            build.append("</text>")
            build.append("</name>")
            build.append("<questiontext format='html'>")
            build.append("<text>")
            build.append("<![CDATA[")
            build.append(question.sentence)
            build.append("]]>")
            build.append("</text>")
            build.append("</questiontext>")
            build.append("<generalfeedback format='html'>")
            build.append("<text></text>")
            build.append("</generalfeedback>")
            build.append("<defaultgrade>1.0000000</defaultgrade>")
            build.append("<penalty>0.3333333</penalty>")
            build.append("<hidden>0</hidden>")
            build.append("<idnumber></idnumber>")
            build.append("<single>true</single>")
            build.append("<shuffleanswers>true</shuffleanswers>")
            build.append("<answernumbering>abc</answernumbering>")
            build.append("<showstandardinstruction>0</showstandardinstruction>")
            build.append("<correctfeedback format='html'>")
            build.append("<text>Your answer is correct.</text>")
            build.append("</correctfeedback>")
            build.append("<partiallycorrectfeedback format='html'>")
            build.append("<text>Your answer is partially correct.</text>")
            build.append("</partiallycorrectfeedback>")
            build.append("<incorrectfeedback format='html'>")
            build.append("<text>Your answer is incorrect.</text>")
            build.append("</incorrectfeedback>")
            val responses = responseRepository.findByQuestion(question)
            val sumPositiveValue = responses.filter { it!!.value!! >= 1 }.sumOf { it!!.value!! }
            val sumNegativeValue = responses.filter { it!!.value!! <= -1 }.sumOf { it!!.value!! }
            var valueToGrade: Float = 0F
            for (response in responses) {
                if (response != null) {
                    if (response.value!! >= 1) {
                        valueToGrade = (response.value!!.toFloat() / sumPositiveValue.toFloat() * 100F)
                    } else
                        valueToGrade = (response.value!!.toFloat() / sumNegativeValue.toFloat() * -100F)
                build.append("<answer fraction='")
                build.append(valueToGrade)
                build.append("' format='html'>")
                build.append("<text>")
                build.append("<![CDATA[")
                build.append(response.proposition)
                build.append("]]>")
                build.append("</text>")
                build.append("<feedback format='html'>")
                build.append("<text>")
                build.append("<![CDATA[")
                build.append(response.feedback)
                build.append("]]>")
                build.append("</text>")
                build.append("</feedback>")
                build.append("</answer>")
            }
        }
    }
    build.append("</question>")
    return build.toString()
}

}
