import parser.Grammar
import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import java.util.*

fun main(args: Array<String>) {
    val p = Parser()
    val tokenizer = Tokenizer()
    val tokens = tokenizer.getTokens("""
class Factorial{
    public static void main(String[] a){
    Fac f;
    int r;
	 f = new Fac();
    r= f.ComputeFac(10);
    }
}

class Fac {

    public int ComputeFac(int num){
	int num_aux ;
	if (num < 1)
	    num_aux = 1 ;
	else 
	    num_aux = num * (this.ComputeFac(num-1)) ;
	return num_aux ;
    }

}
    """.trimIndent()).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
    p.parse(TokensSource(tokens))

//    for (token in tokens){
//        if (token.type!= TokenType.Whitespace) {
//            println(token)
//        }
//    }
}

class TokensSource(private val tokens : Array<Token>) {
    private var index = 0;

    fun accept() {
        index++
    }

    fun isEOF(): Boolean {
        return index >= tokens.size //|| tokens[index] == Token.EOF
    }

    val currentToken: Token
        get() {
            return tokens[index]
        }
    val nextToken: Token
        get() {
            return tokens[index + 1]
        }
    val nextToken2: Token
        get() {
            return tokens[index + 2]
        }
}

class Parser {
    fun parse(ts: TokensSource) {
        val expectedTokens = Stack<Any>()
        expectedTokens.push(Grammar.Program)

        while (true) {
            if (expectedTokens.empty() && ts.isEOF()) {
                break;
            }
            if (expectedTokens.empty() xor ts.isEOF()) {
                println("ERROR!")
                break;
            }

            val expected = expectedTokens.pop()
            val actual = ts.currentToken

            if (expected is Grammar) {
                if (expected == Grammar.StatementMultiple) {
                    if (actual.type == TokenType.BracketCurlyClose || actual.type == TokenType.KeyWordReturn) {

                    } else {
                        expectedTokens.push(Grammar.StatementMultiple)
                        expectedTokens.push(Grammar.Statement)
                    }
                } else if (expected == Grammar.Statement) {
                    when (actual.type) {
                        //'{' statement* '}'
                        TokenType.BracketCurlyOpen -> {
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(Grammar.StatementMultiple)
                            expectedTokens.push(TokenType.BracketCurlyOpen)
                        }
                        //'if' '(' expression ')' statement 'else' statement
                        TokenType.KeyWordIf -> {
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.KeyWordElse)
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                            expectedTokens.push(TokenType.KeyWordIf)
                        }
                        //'while' '(' expression ')' statement
                        TokenType.KeyWordWhile -> {
                            expectedTokens.push(Grammar.Statement)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                            expectedTokens.push(TokenType.KeyWordWhile)
                        }
                        //identifier '=' expression ';'
                        //identifier '[' expression ']' '=' expression ';'
                        TokenType.NameIdentifier -> {
                            if (ts.nextToken.type == TokenType.OperatorAssign) {
                                expectedTokens.push(TokenType.SymbolSemicolon)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.OperatorAssign)
                                expectedTokens.push(TokenType.NameIdentifier)
                            } else if (ts.nextToken.type == TokenType.BracketSquareOpen) {
                                expectedTokens.push(TokenType.SymbolSemicolon)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.OperatorAssign)
                                expectedTokens.push(TokenType.BracketSquareClose)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.BracketSquareOpen)
                                expectedTokens.push(TokenType.NameIdentifier)
                            } else {
                                // accept?
                                println("Unexpected token ${ts.nextToken.type}")
                            }
                        }

                        else -> {
                            println("Unexpected token for Statement ${actual.type}")
                        }
                    }
                } else if (expected == Grammar.Expression) {
                    when (ts.nextToken.type) {
                        //expression ('&&' | '<' | '+' | '-' | '*') expression
                        TokenType.OperatorAnd, TokenType.OperatorLess, TokenType.OperatorPlus, TokenType.OperatorMinus, TokenType.OperatorMult -> {
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(ts.nextToken.type)
                        }
                        //expression '[' expression ']'
                        TokenType.BracketSquareOpen -> {
                            expectedTokens.push(TokenType.BracketSquareClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketSquareOpen)
                        }
                        //expression '.' identifier '(' expression-list? ')'
                        TokenType.SymbolDot -> {
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.ExpressionListOptional)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(TokenType.SymbolDot)
                        }

                        else -> {} // just fine
                    }
                    when (actual.type) {
                        //'(' expression ')'
                        TokenType.BracketRoundOpen -> {
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                        }
                        //'!' expression
                        TokenType.OperatorNot -> {
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.OperatorNot)
                        }
                        //'new' 'int' '[' expression ']'
                        //'new' identifier '(' ')'
                        TokenType.KeyWordNew -> {
                            if (ts.nextToken.type == TokenType.KeyWordInt) {
                                expectedTokens.push(TokenType.BracketSquareClose)
                                expectedTokens.push(Grammar.Expression)
                                expectedTokens.push(TokenType.BracketSquareOpen)
                                expectedTokens.push(TokenType.KeyWordInt)
                                expectedTokens.push(TokenType.KeyWordNew)
                            } else if (ts.nextToken.type == TokenType.NameIdentifier) {
                                expectedTokens.push(TokenType.BracketRoundClose)
                                expectedTokens.push(TokenType.BracketRoundOpen)
                                expectedTokens.push(TokenType.NameIdentifier)
                                expectedTokens.push(TokenType.KeyWordNew)
                            } else println("Unexpected token ${ts.nextToken.type}")
                        }
                        //(integer-literal | 'true' | 'false' | identifier | 'this')
                        TokenType.LiteralNumber -> expectedTokens.push(TokenType.LiteralNumber)
                        TokenType.LiteralTrue -> expectedTokens.push(TokenType.LiteralTrue)
                        TokenType.LiteralFalse -> expectedTokens.push(TokenType.LiteralFalse)
                        TokenType.NameIdentifier -> expectedTokens.push(TokenType.NameIdentifier)
                        TokenType.KeyWordThis -> expectedTokens.push(TokenType.KeyWordThis)

                        else -> println("Unexpected token for Expression ${actual.type}")
                    }
                } else if (expected == Grammar.ExpressionListOptional) {
                    if (actual.type == TokenType.BracketRoundClose) {

                    } else {
                        expectedTokens.push(Grammar.ExpressionList)
                    }
                } else if (expected == Grammar.ExpressionList) {
                    //expression ( ',' expression-list )?
                    if (ts.nextToken.type == TokenType.SymbolComma) {
                        expectedTokens.push(Grammar.ExpressionList)
                        expectedTokens.push(TokenType.SymbolComma)
                    }
                    expectedTokens.push(Grammar.Expression)
                } else if (expected == Grammar.FormalListOptional) {
                    if (actual.type == TokenType.BracketRoundClose) {

                    } else {
                        expectedTokens.push(Grammar.FormalList)
                    }
                } else if (expected == Grammar.FormalList) {
                    //type identifier ( ',' formal-list )?
                    if (ts.nextToken2.type == TokenType.SymbolComma) {
                        expectedTokens.push(Grammar.FormalList)
                        expectedTokens.push(TokenType.SymbolComma)
                    }
                    expectedTokens.push(TokenType.NameIdentifier)
                    expectedTokens.push(Grammar.Type)
                }
                //type identifier ';'
                else if (expected == Grammar.VarDeclaration) {
                    expectedTokens.push(TokenType.SymbolSemicolon)
                    expectedTokens.push(TokenType.NameIdentifier)
                    expectedTokens.push(Grammar.Type)
                } else if (expected == Grammar.VarDeclarationMultiple) {
                    if (ts.nextToken.type == TokenType.NameIdentifier && ts.nextToken2.type == TokenType.SymbolSemicolon) {
                        expectedTokens.push(Grammar.VarDeclarationMultiple)
                        expectedTokens.push(Grammar.VarDeclaration)
                    }
                } else if (expected == Grammar.Type) {
                    //'int' '[' ']'
                    if (actual.type == TokenType.KeyWordInt && ts.nextToken.type == TokenType.BracketSquareOpen) {
                        expectedTokens.push(TokenType.BracketSquareClose)
                        expectedTokens.push(TokenType.BracketSquareOpen)
                    }
                    when (actual.type) {
                        //| ('boolean' | 'int' | identifier)
                        TokenType.KeyWordBoolean -> expectedTokens.push(TokenType.KeyWordBoolean)
                        TokenType.KeyWordInt -> expectedTokens.push(TokenType.KeyWordInt)
                        TokenType.NameIdentifier -> expectedTokens.push(TokenType.NameIdentifier)
                        else -> println("Unexpected token for Type ${actual.type}")
                    }
                } else if (expected == Grammar.MethodDeclaration) {
//                    method-declaration ::=
//                        'public' type identifier '(' formal-list? ')' '{'
//                    var-declaration*
//                    statement*
//                            'return' expression ';'
//                    '}'
                    when (actual.type) {
                        TokenType.KeyWordPublic -> {
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(TokenType.SymbolSemicolon)
                            expectedTokens.push(Grammar.Expression)
                            expectedTokens.push(TokenType.KeyWordReturn)
                            expectedTokens.push(Grammar.StatementMultiple)
                            expectedTokens.push(Grammar.VarDeclarationMultiple)
                            expectedTokens.push(TokenType.BracketCurlyOpen)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(Grammar.FormalListOptional)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(Grammar.Type)
                            expectedTokens.push(TokenType.KeyWordPublic)
                        }

                        else -> println("Unexpected token for MethodDeclaration ${actual.type}")
                    }
                } else if (expected == Grammar.MethodDeclarationMultiple) {
                    if (actual.type == TokenType.BracketCurlyClose) {

                    } else {
                        expectedTokens.push(Grammar.MethodDeclarationMultiple)
                        expectedTokens.push(Grammar.MethodDeclaration)
                    }
                } else if (expected == Grammar.ClassDeclaration) {
//                    'class' identifier ( 'extends' identifier )? '{'
//                    var-declaration*
//                    method-declaration*
//                            '}'
                    when (actual.type) {
                        TokenType.KeyWordClass -> {
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(Grammar.MethodDeclarationMultiple)
                            expectedTokens.push(Grammar.VarDeclarationMultiple)
                            expectedTokens.push(TokenType.BracketCurlyOpen)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(TokenType.KeyWordClass)
                        }

                        else -> println("Unexpected token for ClassDeclaration ${actual.type}")
                    }
                } else if (expected == Grammar.ClassDeclarationMultiple) {
                    if (actual.type == TokenType.EOF) {
                        expectedTokens.push(TokenType.EOF)
                    } else {
                        expectedTokens.push(Grammar.ClassDeclarationMultiple)
                        expectedTokens.push(Grammar.ClassDeclaration)
                    }
                } else if (expected == Grammar.MainClass) {
                    // 'class' identifier '{'
                    //   'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
                    //     statement
                    //   '}'
                    // '}'
                    when (actual.type) {
                        TokenType.KeyWordClass -> {
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(TokenType.BracketCurlyClose)
                            expectedTokens.push(Grammar.StatementMultiple)
                            expectedTokens.push(Grammar.VarDeclarationMultiple)
                            expectedTokens.push(TokenType.BracketCurlyOpen)
                            expectedTokens.push(TokenType.BracketRoundClose)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(TokenType.BracketSquareClose)
                            expectedTokens.push(TokenType.BracketSquareOpen)
                            expectedTokens.push(TokenType.KeyWordString)
                            expectedTokens.push(TokenType.BracketRoundOpen)
                            expectedTokens.push(TokenType.NameIdentifier) //main
                            expectedTokens.push(TokenType.KeyWordVoid)
                            expectedTokens.push(TokenType.KeyWordStatic)
                            expectedTokens.push(TokenType.KeyWordPublic)
                            expectedTokens.push(TokenType.BracketCurlyOpen)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(TokenType.KeyWordClass)
                        }

                        else -> println("Unexpected token for MainClass ${actual.type}")
                    }
                } else if (expected == Grammar.Program) {
                    expectedTokens.push(Grammar.ClassDeclarationMultiple)
                    expectedTokens.push(Grammar.MainClass)
                }
            } else if (expected is TokenType) {
                if (expected == actual.type) {
                    ts.accept()
                } else {
                    println("expected $expected but got ${actual.type}")
                }
            }
        }
    }
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
         | ('boolean' | 'int' | identifier)
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