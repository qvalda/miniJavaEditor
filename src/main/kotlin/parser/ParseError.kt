package parser

import tokenizer.Token

class ParseError(val lineIndex: Int, val token: Token, val message: String)