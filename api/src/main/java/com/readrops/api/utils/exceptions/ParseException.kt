package com.readrops.api.utils.exceptions

class ParseException : Exception {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Exception): super(message, cause)

    constructor(cause: Exception): super(cause)
}