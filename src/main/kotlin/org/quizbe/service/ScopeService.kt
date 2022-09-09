package org.quizbe.service

import org.quizbe.dao.ScopeRepository
import org.quizbe.dao.TopicRepository
import org.quizbe.dao.UserRepository
import org.quizbe.model.Scope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScopeService @Autowired constructor(var topicRepository: TopicRepository, userRepository: UserRepository?, var scopeRepository: ScopeRepository) {
    var logger = LoggerFactory.getLogger(ScopeService::class.java)
    fun save(scope: Scope) {
        scopeRepository.save(scope)
    }

    fun findById(idScope: Long): Optional<Scope?> {
        return scopeRepository.findById(idScope)
    }
}