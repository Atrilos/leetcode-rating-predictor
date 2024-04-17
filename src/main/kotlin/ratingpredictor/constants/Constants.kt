package ratingpredictor.constants

enum class Location(val url: String, val region: String) {
    US("https://leetcode.com", "global"),
    CN("https://leetcode.cn", "local");
}