package ratingpredictor.controller

import lombok.RequiredArgsConstructor
import ratingpredictor.service.PredictorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
class PredictorController(private val predictorService: PredictorService) {

    @GetMapping("/")
    fun readContestResultsIntoDB(@RequestParam(name = "contestName") contestName: String): ResponseEntity<*> {
        predictorService.readContestResultsIntoDB(contestName)
        return ResponseEntity.ok("1")
    }
}