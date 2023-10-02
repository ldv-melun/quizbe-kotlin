package org.quizbe.model

import org.quizbe.utils.Utils
import java.util.stream.Collectors
import javax.persistence.*

@Entity
@Table(name = "topic")
class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = 0

    @Basic
    @Column(name = "name", nullable = false, length = 50)
    var name: String? = null

    @Basic
    @Column(name = "visible", nullable = false)
    var isVisible = true

    @OneToMany(mappedBy = "topic", cascade = [CascadeType.ALL])
    private var scopes = mutableListOf<Scope>()

    @OneToMany(mappedBy = "topic", cascade = [CascadeType.ALL])
    private var questions = mutableListOf<Question>()

    @ManyToOne
    var creator: User? = null

    @ManyToMany(cascade = [CascadeType.PERSIST], fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_topics",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "topic_id", referencedColumnName = "id")]
    )
    var subscribers = mutableSetOf<User>()

    fun getScopes(): MutableList<Scope> {
        return scopes
    }

    fun addScope(scope: Scope) {
        if (!scopes.contains(scope)) {
            scopes.add(scope)
            scope.topic = this
        }
    }

    fun removeScopes() {
        this.scopes.clear();
    }

    fun getQuestions(scope: Scope?, sortBy: String, orderBy: String): MutableList<Question> {
        val comparator =
            when (sortBy) {
                "name" -> if (orderBy == "desc") Utils::compareByNameDesc else Utils::compareByNameAsc
                "designer" -> if (orderBy == "desc") Utils::compareByDesignerDesc else Utils::compareByDesignerAsc
                "date-update" -> if (orderBy == "desc") Utils::compareByUpdateDateDesc else Utils::compareByUpdateDateAsc
                "scope" -> if (orderBy == "desc") Utils::compareByScopeDesc else Utils::compareByScopeAsc
                else -> if (orderBy == "desc") Utils::compareByDesignerDesc else Utils::compareByDesignerAsc
            }
        return if (scope == null) {
            questions.stream().sorted(comparator).collect(Collectors.toList())
        } else {
            questions.stream().filter { question: Question -> question.scope == scope }.sorted(comparator)
                .collect(Collectors.toList())
        }
    }

    fun addSubscribedr(user: User) {
        this.subscribers.add(user)
        user.subscribedTopics.add(this);
    }

    override fun toString(): String {
        return "Topic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", visible=" + isVisible +
                '}'
    }
}
