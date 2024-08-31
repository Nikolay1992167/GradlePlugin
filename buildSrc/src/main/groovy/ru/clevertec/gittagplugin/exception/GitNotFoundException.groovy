package ru.clevertec.gittagplugin.exception

class GitNotFoundException extends RuntimeException {

    GitNotFoundException(String message) {
        super(message)
    }
}