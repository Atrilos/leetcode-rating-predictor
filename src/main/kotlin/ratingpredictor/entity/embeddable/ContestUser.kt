package ratingpredictor.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.util.*

@Embeddable
open class ContestUser {
    @Column(name = "contest_id", nullable = false)
    open var contestId: Int? = null
    @Column(name = "username", nullable = false)
    open var username: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as ContestUser

        return contestId == other.contestId && username == other.username
    }

    override fun hashCode(): Int {
        return Objects.hash(contestId, username)
    }
}