package main.model

interface ICodeSource {
    fun openCode(): String?
    fun saveCode(text: String)
}