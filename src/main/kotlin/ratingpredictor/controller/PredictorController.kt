package ratingpredictor.controller

import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ratingpredictor.service.PredictorService

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
class PredictorController(private val predictorService: PredictorService) {

    @GetMapping("/")
    fun getPredict(
        @RequestParam(name = "contestName") contestName: String,
        @RequestParam(name = "username") username: String
    ): ResponseEntity<*> {
        predictorService.predict(contestName, username)
        return ResponseEntity.ok("1")
    }
}