package org.quizbe.model;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "RESPONSE")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Basic
    @Column(name = "PROPOSITION", nullable = false)
    private String proposition;

    @Basic
    @Column(name = "FEEDBACK", nullable = false)
    private String feedback;

    @Basic
    @Column(name = "VALUE", nullable = false)
    private Integer value;

    @ManyToOne
    private Question question;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProposition() {
        return proposition;
    }

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return getId() == response.getId() && getProposition().equals(response.getProposition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProposition());
    }

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", proposition='" + proposition + '\'' +
                ", feedback='" + feedback + '\'' +
                ", value=" + value +
                '}';
    }
}
