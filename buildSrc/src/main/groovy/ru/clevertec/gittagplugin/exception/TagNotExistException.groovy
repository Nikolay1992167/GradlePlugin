package ru.clevertec.gittagplugin.exception

class TagNotExistException extends RuntimeException {

    TagNotExistException(String message) {
        super(message)
    }
}