package org.quizbe.dao;

import org.quizbe.model.Topic;
import org.quizbe.model.Scope;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScopeRepository extends CrudRepository<Scope, Long> {

    List<Scope> findByTopic(Topic topic);
    // Scope findById(long id);

}
