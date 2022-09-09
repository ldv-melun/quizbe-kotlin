package org.quizbe.dao

import org.quizbe.model.Scope
import org.quizbe.model.Topic
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScopeRepository : CrudRepository<Scope?, Long?> {
    fun findByTopic(topic: Topic?): List<Scope?>? // Scope findById(long id);
}