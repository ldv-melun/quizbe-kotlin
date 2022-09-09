package org.quizbe.service

import org.quizbe.dao.QuestionRepository
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
class QuestionService @Autowired constructor(private val questionRepository: QuestionRepository, private val scopeRepository: ScopeRepository) {
    var logger  : Logger = LoggerFactory.getLogger(QuestionService::class.java)
    fun saveQuestionFromQuestionDto(questionDto: QuestionDto): Boolean {
        val question = convertToQuestion(questionDto)
        questionRepository.save(question)
        return true
    }

    private fun convertToQuestion(questionDto: QuestionDto): Question {
        val question: Question
        if (questionDto.id != null) {
            question = questionRepository.findById(questionDto.id!!.toLong()).orElseThrow { QuestionNotFoundException() }!!
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

    fun findNextById(id: Long): Question? {
        val idNext = questionRepository.findNextById(id)
        logger.info("id next :$idNext")
        return if (idNext != null) findById(idNext.toLong()) else null
    }

    fun delete(question: Question) {
        questionRepository.delete(question)
    }
}