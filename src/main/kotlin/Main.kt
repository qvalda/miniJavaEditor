import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import java.util.Stack

fun main(args: Array<String>) {
    val p = Parser()
    val tokenizer = Tokenizer()
    val tokens = tokenizer.getTokens("""
        while(true)
        {
            while(true)
            {
                a = 1;
                b = 1;
            }
        }""").filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
    p.parse(TokensSource(tokens))
}

class TokensSource(private val tokens : Array<Token>) {
    private var index = 0;
    public fun getNextToken() : Token{
        return tokens[index++]
    }
    public fun peek2Token() : Token{
        return tokens[index]
    }

    public fun stepBack(){
        index--
    }
}

class Parser{
    fun parse(ts : TokensSource) {
        val expectedTokens = Stack<Any>()
        expectedTokens.push(Grammar.Statement)

        while (true){
            if(expectedTokens.empty()) {
                break;
            }
            val expected = expectedTokens.pop()
            val actual = ts.getNextToken()

            if (expected is Grammar) {
                if(expected == Grammar.StatementMultiple){
                    if (actual.type == TokenType.BracketCurlyClose) {
                        ts.stepBack()
                    }
                    else{
                        expectedTokens.push(Grammar.StatementMultiple)
                        expectedTokens.push(Grammar.Statement)
                        ts.stepBack()
                    }
                }
                else if(expected == Grammar.Statement){
//                    statement ::= '{' statement* '}'
//                    | 'if' '(' expression ')' statement 'else' statement
//                    | 'while' '(' expression ')' statement
//                    | 'System''.''out''.''println' '(' expression ')' ';'
//                    | identifier '=' expression ';'
//                    | identifier '[' expression ']' '=' expression ';'
                    when (actual.type) {
                        TokenType.BracketCurlyOpen -> {
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(Grammar.StatementMultiple)
                        }
                        TokenType.KeyWordIf -> {
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.KeyWordElse)
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                        }
                        TokenType.KeyWordWhile -> {
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                        }
                        TokenType.NameIdentifier -> {
                            if (ts.peek2Token().type == TokenType.OperatorAssign) {
                                expectedTokens.push(TokenType.SymbolSemicolon)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.OperatorAssign)
                            }
                            else if(ts.peek2Token().type == TokenType.BracketSquareOpen){
                                expectedTokens.push(TokenType.SymbolSemicolon)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.OperatorAssign)
                                expectedTokens.push(TokenType.BracketSquareClose)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.BracketSquareOpen)
                            }
                            else{
                                println("Unexpected token ${ts.peek2Token().type}")
                            }
                        }
                        else -> continue
                    }
                }
                else if (expected == Grammar.Expression) {
                    if (ts.peek2Token().type in arrayOf(
                            TokenType.OperatorAnd,
                            TokenType.OperatorLess,
                            TokenType.OperatorPlus,
                            TokenType.OperatorMinus,
                            TokenType.OperatorMult
                        )
                    ) {
                        expectedTokens.push(Grammar.Expression)
                        expectedTokens.push(ts.peek2Token().type)
                    } else if (ts.peek2Token().type == TokenType.BracketSquareOpen) { // | expression '[' expression ']'
                        expectedTokens.push(TokenType.BracketSquareClose)
                        expectedTokens.push(Grammar.Expression)
                        expectedTokens.push(TokenType.BracketSquareOpen)
                    } else if (ts.peek2Token().type == TokenType.SymbolDot) { // | expression '.' identifier '(' expression-list? ')'
                        expectedTokens.push(TokenType.BracketRoundClose)
                        expectedTokens.push(Grammar.ExpressionListOptional)
                        expectedTokens.push(TokenType.BracketRoundOpen)
                        expectedTokens.push(TokenType.NameIdentifier)
                        expectedTokens.push(TokenType.SymbolDot)
                    }
                    when (actual.type) {
                        TokenType.BracketRoundOpen -> {
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                        }

                        TokenType.OperatorNot -> {
                            expectedTokens.push(Grammar.Expression)
                        }

                        TokenType.KeyWordNew -> {
                            if (ts.peek2Token().type == TokenType.KeyWordInt) {
                                expectedTokens.push(TokenType.BracketSquareClose)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.BracketSquareOpen)
                                expectedTokens.push(TokenType.KeyWordInt)
                            } else {
                                expectedTokens.push(TokenType.BracketRoundClose)
                                expectedTokens.push(TokenType.BracketRoundOpen)
                                expectedTokens.push(TokenType.NameIdentifier)
                            }
                        }

                        TokenType.LiteralNumber, TokenType.LiteralTrue, TokenType.LiteralFalse, TokenType.NameIdentifier, TokenType.KeyWordThis -> {}
                        else -> println("Unexpected token ${actual.type}")
                    }
                }
                else if (expected == Grammar.ExpressionListOptional) {
                    if(actual.type == TokenType.BracketRoundClose){
                        ts.stepBack()
                    }
                    else{
                        expectedTokens.push(Grammar.ExpressionList)
                        ts.stepBack()
                    }
                }
                else if(expected == Grammar.ExpressionList){
                    if(ts.peek2Token().type == TokenType.SymbolComma){
                        expectedTokens.push(Grammar.ExpressionList)
                        expectedTokens.push(TokenType.SymbolComma)
                    }
                    expectedTokens.push(Grammar.Expression)
                    ts.stepBack()
                }
            }
            else if(expected is TokenType){
                if(expected == actual.type){
                    continue
                }
                else{
                    println("expected $expected but got ${actual.type}")
                }
            }
        }
    }

//    fun isExpressionBeginning(token: Token) : Boolean{
//
//    }

}

enum class Grammar{
    Statement,
    StatementMultiple,
    Expression,
    ExpressionList,
    ExpressionListOptional,
}

/*
START:
  start ::= program
P:
  program ::= main-class class-declaration*
MC:
  main-class ::=
    'class' identifier '{'
      'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
        statement
      '}'
    '}'
CD:
  class-declaration ::=
    'class' identifier ( 'extends' identifier )? '{'
      var-declaration*
      method-declaration*
    '}'
VD:
  var-declaration ::= type identifier ';'
MD:
  method-declaration ::=
    'public' type identifier '(' formal-list? ')' '{'
      var-declaration*
      statement*
      'return' expression ';'
    '}'
T:
  type ::= 'int' '[' ']'
         | 'boolean'
         | 'int'
         | identifier
FL:
  formal-list ::=
    type identifier ( ',' formal-list )?
S:
  statement ::= '{' statement* '}'
              | 'if' '(' expression ')' statement 'else' statement
              | 'while' '(' expression ')' statement
              | 'System''.''out''.''println' '(' expression ')' ';'
              | identifier '=' expression ';'
              | identifier '[' expression ']' '=' expression ';'
E:
  expression ::= expression ('&&' | '<' | '+' | '-' | '*') expression
               | expression '[' expression ']'
               | expression '.' 'length'
               | expression '.' identifier '(' expression-list? ')'
               | (integer-literal | 'true' | 'false' | identifier | 'this')
               | 'new' 'int' '[' expression ']'
               | 'new' identifier '(' ')'
               | '!' expression
               | '(' expression ')'
EL:
  expression-list ::=
    expression ( ',' expression-list )?
 */

/*
ID:
  identifier ::= letter ( letter | digit | '_' )*
IL:
  integer-literal ::= digit+

  letter ::= 'a'-'z' | 'A'-'Z'

  digit ::= '0'-'9'
 */