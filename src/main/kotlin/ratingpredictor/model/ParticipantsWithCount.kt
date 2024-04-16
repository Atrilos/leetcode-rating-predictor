package ratingpredictor.model

import jakarta.persistence.*
import ratingpredictor.model.embeddable.ContestNamePage

@Entity
@Table(name = "participant_with_count")
open class ParticipantsWithCount {
    @EmbeddedId
    open var contestNamePage: ContestNamePage? = null

    @OneToMany(mappedBy = "totalRank", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var totalRank: MutableSet<Participant> = mutableSetOf()

    @Column(name = "user_num", nullable = false)
    open var userNum: Int? = null
}

