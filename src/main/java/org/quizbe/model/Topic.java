package org.quizbe.model;


import org.quizbe.service.ScopeService;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "TOPIC")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private long id;

    @Basic
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Basic
    @Column(nullable = false)
    private boolean visible = true;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Scope> scopes;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Question> questions;

    @ManyToOne
    private User creator;

    public Topic() {
        this.scopes = new ArrayList<Scope>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public void addScope(Scope scope) {
        if (!this.scopes.contains(scope)) {
            this.scopes.add(scope);
            scope.setTopic(this);
        }
    }
    public void removeScope(Scope scope) {
        scope.setTopic(null);
        this.scopes.remove(scope);
    }

    public void removeScopes() {
        Iterator<Scope> it = this.scopes.iterator();
        while (it.hasNext()) {
            Scope scope = it.next();
            scope.setTopic(null);
            it.remove();
        }
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Question> getQuestions(Scope scope) {
        if (scope == null) {
            return questions;
        }
        return  this.questions.stream().filter(question -> question.getScope().equals(scope)).collect(Collectors.toList());
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", visible=" + visible +
                '}';
    }
}
