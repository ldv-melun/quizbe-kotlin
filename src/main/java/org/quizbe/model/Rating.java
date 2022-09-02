package org.quizbe.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "RATING",  uniqueConstraints={@UniqueConstraint(columnNames={"question_id", "user_id"})})
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Basic
    @Column(nullable = true)
    private String comment;

    @Basic
    @Column(name = "VALUERATING", nullable = false)
    private Integer value;

    @Basic
    @Column(nullable = false)
    private LocalDateTime dateUpdate;

    @ManyToOne
    private Question question;

    @ManyToOne
    private User user;

    public Rating(Long id, String comment, Integer value, LocalDateTime dateUpdate, Question question, User user) {
        this.id = id;
        this.comment = comment;
        this.value = value;
        this.dateUpdate = dateUpdate;
        this.question = question;
        this.user = user;
    }

    public Rating() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public LocalDateTime getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(LocalDateTime dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isOutDated() {
        return question.getDateUpdate().isAfter(this.getDateUpdate());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return getId() == rating.getId() && Objects.equals(getComment(), rating.getComment()) && getValue().equals(rating.getValue()) && getDateUpdate().equals(rating.getDateUpdate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getComment(), getValue(), getDateUpdate());
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", value=" + value +
                ", dateUpdate=" + dateUpdate +
                '}';
    }
}
