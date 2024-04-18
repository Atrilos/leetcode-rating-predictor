package ratingpredictor.model

import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import org.springframework.web.bind.annotation.GetMapping

@Entity
@Table(name = "contest")
open class Contest {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @NaturalId
    open var contestName: String? = null

    @OneToMany(mappedBy = "contest", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var participants: MutableSet<Participant> = mutableSetOf()

    @Column(name = "time")
    open var time: Double? = null
}

