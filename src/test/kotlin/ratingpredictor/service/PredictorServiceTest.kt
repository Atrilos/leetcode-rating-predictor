package ratingpredictor.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ratingpredictor.configurations.HttpClientConfiguration
import ratingpredictor.constants.Location
import ratingpredictor.dto.ContestDto
import java.io.*

class PredictorServiceTest {


    private val objectMapper = ObjectMapper()
    private val out = PredictorService(objectMapper, HttpClientConfiguration().httpClient())
    private val basicContest = "weekly-contest-392"

    @Test
    @Disabled
    fun fetchUsers() {
        val contest = deserialize("contestDto.dat")
        contest as ContestDto
        contest.participants
        val fetchUsers = out.fetchUsers(ArrayList(contest.participants.subList(0, 100)))
        println()
    }

    @Test
    @Disabled
    fun fetchContest() {
        val contestDto = out.fetchContest(basicContest)

        serialize(contestDto, "contestDto.dat")
    }

    @Test
    fun predict() {
        println(objectMapper.writeValueAsString(out.predict("weekly-contest-393")))
    }

    private fun serialize(o: Any, fileName: String) {
        FileOutputStream(fileName).use { fileOutputStream ->
            ObjectOutputStream(fileOutputStream).use { oos ->
                oos.writeObject(o)
            }
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun deserialize(fileName: String): Any {
        FileInputStream(fileName).use { fileInputStream ->
            ObjectInputStream(fileInputStream).use { ois ->
                return ois.readObject()
            }
        }
    }
}