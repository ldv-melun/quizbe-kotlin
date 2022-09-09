package org.quizbe.dao

import org.quizbe.model.Topic
import org.quizbe.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TopicRepository : CrudRepository<Topic?, Long?> {
    fun findByName(nName: String?): List<Topic?>?

    //Topic findById(long id);
    fun findByCreator(user: User?): List<Topic?>?
}