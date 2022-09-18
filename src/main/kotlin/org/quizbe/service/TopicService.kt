package org.quizbe.service

import com.google.common.collect.Lists
import org.quizbe.dao.ScopeRepository
import org.quizbe.dao.TopicRepository
import org.quizbe.dao.UserRepository
import org.quizbe.dto.ScopeDto
import org.quizbe.dto.TopicDto
import org.quizbe.exception.TopicNotFoundException
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.Scope
import org.quizbe.model.Topic
import org.quizbe.model.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import java.util.*
import java.util.stream.Collectors

@Service
class TopicService @Autowired constructor(var topicRepository: TopicRepository, var userRepository: UserRepository, var scopeRepository: ScopeRepository) {
    var logger = LoggerFactory.getLogger(TopicService::class.java)
    val allTopics: List<Topic?>
        get() = Lists.newArrayList(topicRepository.findAll())

    fun getAllTopicsOf(user: User?): List<Topic?>? {
        return topicRepository.findByCreator(user)
    }

    fun saveTopicFromTopicDto(topicDto: TopicDto, result: BindingResult?): Boolean {
        // rechercher si il n'existe pas une classe de même nom associée au courrent user
        val creator = userRepository.findByUsername(topicDto.creatorUsername)
                ?: throw UserNotFoundException("Creator not found :" + topicDto.creatorUsername)
        val topics = topicRepository.findByCreator(creator)

        // create topic ?
        if (topics != null && topicDto.id == null) {
            if (topics.stream().anyMatch { classroom: Topic? -> classroom!!.name.equals(topicDto.name, ignoreCase = true) }) {
                // a topic exists whith same name
                result?.rejectValue("name", "topic.name.already.exists", "already exists")
                return false
            }
        }
        val topic: Topic
        if (topicDto.id != null) {
            // update
            topic = topicRepository.findById(topicDto.id!!).orElseThrow { TopicNotFoundException() }!!
            topic.removeScopes()
            topic.name = topicDto.name
        } else {
            topic = Topic()
            topic.name = topicDto.name
            topic.creator = creator
        }
        // add/update all scopes
        for (scopeDto in topicDto.getScopesDtos()) {
            val scope = Scope()
            scope.name = scopeDto.name
            if (scopeDto.id != null) {
                scope.id = scopeDto.id
            }
            topic.addScope(scope)
            // logger.info("scope add : " + scope);
        }
        topic.isVisible = topicDto.isVisible
        topicRepository.save(topic)
        return true
    }

    fun findTopicDtoById(id: Long): TopicDto {
        val topic = topicRepository.findById(id)
        return if (topic.isPresent) {
            fromTopicToTopicDto(topic.get())
        } else {
            throw TopicNotFoundException("Invalid topic Id:$id")
        }
    }

    private fun fromTopicToTopicDto(topic: Topic): TopicDto {
        val topicDto = TopicDto()
        topicDto.id = topic.id
        topicDto.name = topic.name
        topicDto.isVisible = topic.isVisible
        topicDto.creatorUsername = topic.creator!!.username
        topicDto.setScopesDtos(topic.getScopes().stream().map { scope: Scope -> ScopeDto(scope.id, scope.name) }.collect(Collectors.toList()))
        return topicDto
    }

    fun findTopicById(id: Long): Optional<Topic?> {
        return topicRepository.findById(id)
    }

    fun findById(idClassroom: Long): Optional<Topic?> {
        return topicRepository.findById(idClassroom)
    }

    fun deleteTopic(topic: Topic) {
        for (subscriber in topic.subscribers) {
            subscriber.subscribedTopics.remove(topic)
        }
        topicRepository.delete(topic)
    }

    fun getTopicById(i: Long): Optional<Topic?> {
        return topicRepository.findById(i)
    }

    fun save(topic: Topic) {
        logger.info("Topic : $topic")
        topicRepository.save(topic)
    }
}