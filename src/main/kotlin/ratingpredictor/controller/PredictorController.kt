package ratingpredictor.controller

import lombok.RequiredArgsConstructor
import ratingpredictor.service.PredictorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ratingpredictor.service.UserService

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
class PredictorController(private val predictorService: PredictorService,
    private val userService: UserService
) {

    @GetMapping("/")
    fun readContestResultsIntoDB(@RequestParam(name = "contestName") contestName: String): ResponseEntity<*> {
        predictorService.readContestResultsIntoDB(contestName)
        return ResponseEntity.ok("1")
    }

    @GetMapping("/user")
    fun retrieveUserRatingUS(): ResponseEntity<*> {
        userService.retrieveUserRatingUS("atrilos")
        return ResponseEntity.ok("1")
    }

    @GetMapping("/user2")
    fun retrieveUserRatingUS2(): ResponseEntity<*> {
        userService.getStats("atrilos")
        return ResponseEntity.ok("1")
    }
}