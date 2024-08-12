package ru.clevertec.gittagplugin.builder

import ru.clevertec.gittagplugin.model.SortOrder

import static ru.clevertec.gittagplugin.util.Constants.GIT;
import static ru.clevertec.gittagplugin.util.Constants.VERSION;
import static ru.clevertec.gittagplugin.util.Constants.DESCRIBE;
import static ru.clevertec.gittagplugin.util.Constants.TAG;
import static ru.clevertec.gittagplugin.util.Constants.TAGS;
import static ru.clevertec.gittagplugin.util.Constants.ABBREV;
import static ru.clevertec.gittagplugin.util.Constants.BRANCH;
import static ru.clevertec.gittagplugin.util.Constants.SHOW_CURRENT;
import static ru.clevertec.gittagplugin.util.Constants.PUSH;
import static ru.clevertec.gittagplugin.util.Constants.ORIGIN;
import static ru.clevertec.gittagplugin.util.Constants.DIFF;
import static ru.clevertec.gittagplugin.util.Constants.LIST;
import static ru.clevertec.gittagplugin.util.Constants.SORT;

class CommandBuilder {

    List<String> commands

    private CommandBuilder() {
        commands = new ArrayList<>()
    }

    static def builder() {
        return new CommandBuilder()
    }

    CommandBuilder git() {
        this.command(GIT)
    }

    CommandBuilder version() {
        this.command(VERSION)
    }

    CommandBuilder describe() {
        this.command(DESCRIBE)
    }

    CommandBuilder tag() {
        this.command(TAG)
    }

    CommandBuilder tags() {
        this.command(TAGS)
    }

    CommandBuilder abbrev(int number) {
        this.command("$ABBREV$number")
    }

    CommandBuilder branch() {
        this.command(BRANCH)
    }

    CommandBuilder showCurrent() {
        this.command(SHOW_CURRENT)
    }

    CommandBuilder push() {
        this.command(PUSH)
    }

    CommandBuilder origin() {
        this.command(ORIGIN)
    }

    CommandBuilder diff() {
        this.command(DIFF)
    }

    CommandBuilder list() {
        this.command(LIST)
    }

    CommandBuilder sort(String by, SortOrder order) {
        this.command("$SORT${order.getName()}$by")
    }

    CommandBuilder command(String command) {
        this.commands.add(command)
        this
    }

    String execute() {
        def errorBuilder = new StringBuilder()
        def process = this.commands.execute()
        process.consumeProcessErrorStream(errorBuilder)
        def result = process.in
                .text
                .trim()
        errorBuilder.isEmpty()
                ? result
                : errorBuilder.toString().trim()
    }
}