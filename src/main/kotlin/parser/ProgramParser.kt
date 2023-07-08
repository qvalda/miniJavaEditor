package parser

class ProgramParser : IParser {
    override fun parse(ts: ITokenSource): ParserResult {
        return RecursiveParser(ts).parse()
    }
}