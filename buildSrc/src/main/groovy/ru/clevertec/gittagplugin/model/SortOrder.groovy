package ru.clevertec.gittagplugin.model

enum SortOrder {

    ASC(''), DESC('-')

    private String name

    SortOrder(String name) {
        this.name = name
    }

    String getName() {
        return name
    }
}