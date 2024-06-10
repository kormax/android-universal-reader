package com.kormax.universalreader

enum class ValueAddedServicesStatus {
    // Value added services are unavailable
    UNAVAILABLE,
    // User has to authenticate
    WAITING_FOR_AUTHENTICATION,
    // User has to select appropriate service
    WAITING_FOR_SELECTION,
    // Failure, when data is missing
    DATA_NOT_FOUND,
    // Error, such as wrong request or response format
    ERROR,
    // Success
    SUCCESS,
}
