package org.quizbe.model;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Entity
    //  @DynamicInsert
   // see https://thorben-janssen.com/dynamic-inserts-and-updates-with-spring-data-jpa/
   // https://stackoverflow.com/questions/21721818/why-does-not-hibernate-set-dynamicinsert-by-default
   // Question may be updated frequently (Response also, to test before :)
@Table(name = "QUESTION")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private long id;

    @Basic
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Lob
    @Column(name = "SENTENCE", nullable = false)
    private String sentence;

    @Basic
    @Column(nullable = false)
    private LocalDateTime dateUpdate;

    /// trace de la personne conceptrice et co-conceptrice
    @Basic
    @Column(name = "DESIGNER", nullable = false, length = 50)
    private String designer;

    @Basic
    @Column(name = "CODESIGNER", nullable = false, length = 50)
    private String codesigner;

    @Basic
    @Column(nullable = false)
    private boolean visible = true;

    ///

    @ManyToOne
    private Scope scope;

    @ManyToOne
    private Topic topic;

    @OneToMany(mappedBy = "question",  orphanRemoval = true, cascade = { CascadeType.ALL })
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "question", orphanRemoval = true,   cascade = { CascadeType.ALL })
    private List<Response> responses = new ArrayList<>();

    /**
     * Get number of expected good responses
     * @return number of responses which are value > 0
     */
    public int getExpectedGoodChoices() {
        return (int) this.responses.stream().filter(response -> response.getValue() >= 0).count();
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

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public LocalDateTime getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(LocalDateTime datecrea) {
        this.dateUpdate = datecrea;
    }

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public String getCodesigner() {
        return codesigner;
    }

    public void setCodesigner(String codesigner) {
        this.codesigner = codesigner;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    /**
     * get average ratings
     * @return
     */
    public int getAvgRatings() {
        return this.ratings.size()==0 ? 0 : this.ratings.stream().mapToInt(i -> i.getValue()).sum() / this.ratings.size();
    }

    public void removeResponses() {
        Iterator<Response> it = this.responses.iterator();
        while (it.hasNext()) {
            Response r = it.next();
            r.setQuestion(null);
            it.remove();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question scope = (Question) o;
        return getId() == scope.getId() && getName().equals(scope.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sentence='" + sentence + '\'' +
                ", datecrea=" + dateUpdate +
                ", designer='" + designer + '\'' +
                ", codesigner='" + codesigner + '\'' +
                ", visible='" + visible + '\'' +
                '}';
    }


    public void addResponse(Response response) {
        if (!this.responses.contains(response)) {
            this.responses.add(response);
            response.setQuestion(this);
        }
    }
}
