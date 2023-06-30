package models

interface ICodeSource {
    fun openCode(): String?
    fun saveCode(text: String)
}