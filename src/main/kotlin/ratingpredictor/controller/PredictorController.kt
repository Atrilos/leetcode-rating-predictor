package ratingpredictor.controller

import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ratingpredictor.constants.Location
import ratingpredictor.dto.UserDataDto
import ratingpredictor.exceptions.WrongRegionException
import ratingpredictor.service.PredictorService

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
class PredictorController(private val predictorService: PredictorService) {

    @GetMapping("/")
    fun getPredict(
        @RequestParam(name = "contestName") contestName: String,
        @RequestParam(name = "username") username: String,
        @RequestParam(name = "region") region: String
    ): ResponseEntity<List<UserDataDto>> {
        if (region.uppercase() !in Location.entries.map { it.name }) {
            throw WrongRegionException("Region not found. Available regions: ${Location.entries.joinToString(", ") { it.name }}")
        }
        return ResponseEntity.ok(predictorService.predict(contestName))
    }
}