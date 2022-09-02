package org.quizbe.model;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "SCOPE")
public class Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Basic
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;

    public Scope() {

    }

    public Scope(String name, Topic topic) {
        this.name = name;
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scope scope = (Scope) o;

        return getId() == scope.getId() && getName().equals(scope.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Scope{"
                + "id=" + id
                + ", name='" + name + '\''
                + '}';
    }
}
