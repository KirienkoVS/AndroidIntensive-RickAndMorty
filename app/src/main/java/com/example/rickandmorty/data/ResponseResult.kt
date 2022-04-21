package com.example.rickandmorty.data

data class ResponseResult<out T>(val status: Status, val response: T?, val message: String?) {
    companion object {
        fun <T> success(response: T?): ResponseResult<T> {
            return ResponseResult(Status.SUCCESS, response, null)
        }

        fun <T> error(message: String?, response: T?): ResponseResult<T> {
            return ResponseResult(Status.ERROR, response, message)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR
}
