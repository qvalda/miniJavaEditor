package parser

interface IParser {
    fun parse(ts: ITokenSource): ParserResult
}