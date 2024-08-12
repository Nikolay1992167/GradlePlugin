package ru.clevertec.gittagplugin.util

interface Constants {

    def LOGO = $/
##############################################################################################################
Git Tag Plugin from Nikolay Minich!
##############################################################################################################
/$

    def PUSH_TAG = 'pushTag'
    def DEFAULT_TAG_VERSION = 'v0.1'
    def RC = '-rc'
    def SNAPSHOT = '-SNAPSHOT'

    def GIT = 'git'
    def VERSION = 'version'
    def DESCRIBE = 'describe'
    def TAG = 'tag'
    def TAGS = '--tags'
    def ABBREV = '--abbrev='
    def BRANCH = 'branch'
    def SHOW_CURRENT = '--show-current'
    def PUSH = 'push'
    def ORIGIN = 'origin'
    def DIFF = 'diff'
    def LIST = '-l'
    def SORT  = '--sort='
}