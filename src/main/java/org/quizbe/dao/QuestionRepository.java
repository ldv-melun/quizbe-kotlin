package org.quizbe.dao;

import org.quizbe.model.Question;
import org.quizbe.model.Topic;
import org.quizbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

  @Query("select min(id) from Question where id > :id ")
  Integer findNextById(long id);

}
