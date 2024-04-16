package ratingpredictor.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.util.*

@Embeddable
open class ContestNamePage {
    @Column(name = "contest_name", nullable = false)
    open var contestName: String? = null
    @Column(name = "page_num", nullable = false)
    open var pageNum: Int? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as ContestNamePage

        return contestName == other.contestName && pageNum == other.pageNum
    }

    override fun hashCode(): Int {
        return Objects.hash(contestName, pageNum)
    }
}