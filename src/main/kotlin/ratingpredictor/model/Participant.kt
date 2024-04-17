package ratingpredictor.model

import jakarta.persistence.*
import lombok.ToString
import lombok.extern.slf4j.Slf4j
import org.hibernate.Hibernate
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ratingpredictor.model.embeddable.ContestUser
import java.util.*

@Entity
@ToString
@Slf4j
@Table(name = "participant")
open class Participant {
    @EmbeddedId
    open var id: ContestUser? = null

    @Column(name = "rank", nullable = false)
    open var rank: Int? = null

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "score", nullable = false)
    open var score: Int? = null


    @Column(name = "finish_time", nullable = false)
    open var finishTime: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contest_name")
    open var contest: Contest? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as Participant

        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

}