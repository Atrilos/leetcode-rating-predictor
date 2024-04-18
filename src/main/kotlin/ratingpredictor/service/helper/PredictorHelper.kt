package ratingpredictor.service.helper

import ratingpredictor.dto.UserDataDto
import kotlin.math.pow
import kotlin.math.sqrt

fun getExpectedRank(userList: List<UserDataDto>, currentUserRating: Double): Double {
    var seed = 0.0

    for (user in userList) {
        seed += meanWinningPercentage(user.currentRating, currentUserRating);
    }

    return seed;
}

fun meanWinningPercentage(a: Double, b: Double): Double {
    return 1.0 / (1 + 10.0.pow((b - a) / 400))
}

fun geometricMean(expectedRank: Double, currentUserRating: Double): Double = sqrt(expectedRank * currentUserRating)

fun getRating(userList: List<UserDataDto>, gMean: Double): Double {
    var l = 1.0
    var r = 1e6
    var mid = 0.0
    var seed: Double

    while (r - l > 0.1) {
        mid = l + (r - l) / 2;
        seed = 1 + getExpectedRank(userList, mid);
        if (seed > gMean) {
            l = mid; // to reduce seed -> increase ERating
        } else {
            r = mid; // to increase seed -> decrease ERating
        }
    }

    return mid;
}
