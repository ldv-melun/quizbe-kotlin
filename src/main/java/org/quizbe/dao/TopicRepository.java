package org.quizbe.dao;

import org.quizbe.model.Topic;
import org.quizbe.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TopicRepository extends CrudRepository<Topic, Long> {

    List<Topic> findByName(String nName);

    //Topic findById(long id);

    List<Topic> findByCreator(User user);
}
