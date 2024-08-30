package ru.clevertec.gittagplugin.exception

class UncommittedChangesException extends RuntimeException {

    UncommittedChangesException(String message) {
        super(message)
    }
}