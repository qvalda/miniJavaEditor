package parser

import TokensSource
import tokenizer.TokenType
import java.util.*

/*
program ::= main-class class-declaration*
main-class ::=
    'class' identifier '{'
        'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
            var-declaration*
            statement*
        '}'
    '}'
class-declaration ::=
    'class' identifier ( 'extends' identifier )? '{'
        var-declaration*
        method-declaration*
    '}'
var-declaration ::= type identifier ';'
method-declaration ::=
    'public' type identifier '(' formal-list? ')' '{'
        var-declaration*
        statement*
        'return' expression ';'
    '}'
type ::= 'int' '[' ']'
       | ('boolean' | 'int' | identifier)
formal-list ::= type identifier ( ',' formal-list )?
statement ::= '{' statement* '}'
            | 'if' '(' expression ')' statement 'else' statement
            | 'while' '(' expression ')' statement
            | 'System''.''out''.''println' '(' expression ')' ';'
            | identifier '=' expression ';'
            | identifier '[' expression ']' '=' expression ';'
expression ::= expression ('&&' | '<' | '+' | '-' | '*') expression
             | expression '[' expression ']'
             | expression '.' 'length'
             | expression '.' identifier '(' expression-list? ')'
             | (integer-literal | 'true' | 'false' | identifier | 'this')
             | 'new' 'int' '[' expression ']'
             | 'new' identifier '(' ')'
             | '!' expression
             | '(' expression ')'
expression-list ::= expression ( ',' expression-list )?
 */

class Parser {
    fun parse(ts: TokensSource, entryPoint: Grammar = Grammar.Program) {
        val expectedTokens = Stack<Any>()
        expectedTokens.push(entryPoint)

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

            when (expected) {
                is Grammar -> {
                    when (expected) {
                        Grammar.Program -> {
                            expectedTokens.push(Grammar.ClassDeclarationMultiple)
                            expectedTokens.push(Grammar.MainClass)
                        }

                        Grammar.MainClass -> {
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
                        }

                        Grammar.ClassDeclaration -> {
                            //    'class' identifier ( 'extends' identifier )? '{'
                            //      var-declaration*
                            //      method-declaration*
                            //    '}'
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
                        }

                        Grammar.ClassDeclarationMultiple -> {
                            if (actual.type == TokenType.EOF) {
                                expectedTokens.push(TokenType.EOF)
                            } else {
                                expectedTokens.push(Grammar.ClassDeclarationMultiple)
                                expectedTokens.push(Grammar.ClassDeclaration)
                            }
                        }

                        Grammar.MethodDeclaration -> {
                            //    'public' type identifier '(' formal-list? ')' '{'
                            //      var-declaration*
                            //      statement*
                            //      'return' expression ';'
                            //    '}'
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
                        }

                        Grammar.MethodDeclarationMultiple -> {
                            if (actual.type != TokenType.BracketCurlyClose) {
                                expectedTokens.push(Grammar.MethodDeclarationMultiple)
                                expectedTokens.push(Grammar.MethodDeclaration)
                            }
                        }

                        Grammar.VarDeclaration -> {
                            //type identifier ';'
                            expectedTokens.push(TokenType.SymbolSemicolon)
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(Grammar.Type)
                        }

                        Grammar.VarDeclarationMultiple -> {
                            if (ts.nextToken.type == TokenType.NameIdentifier && ts.nextToken2.type == TokenType.SymbolSemicolon) {
                                expectedTokens.push(Grammar.VarDeclarationMultiple)
                                expectedTokens.push(Grammar.VarDeclaration)
                            }
                        }

                        Grammar.Type -> {
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
                        }

                        Grammar.FormalList -> {
                            //type identifier ( ',' formal-list )?
                            if (ts.nextToken2.type == TokenType.SymbolComma) {
                                expectedTokens.push(Grammar.FormalList)
                                expectedTokens.push(TokenType.SymbolComma)
                            }
                            expectedTokens.push(TokenType.NameIdentifier)
                            expectedTokens.push(Grammar.Type)
                        }

                        Grammar.FormalListOptional -> {
                            if (actual.type != TokenType.BracketRoundClose) {
                                expectedTokens.push(Grammar.FormalList)
                            }
                        }

                        Grammar.Statement -> {
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
                                    when (ts.nextToken.type) {
                                        TokenType.OperatorAssign -> {
                                            expectedTokens.push(TokenType.SymbolSemicolon)
                                            expectedTokens.push(Grammar.Expression)
                                            expectedTokens.push(TokenType.OperatorAssign)
                                            expectedTokens.push(TokenType.NameIdentifier)
                                        }

                                        TokenType.BracketSquareOpen -> {
                                            expectedTokens.push(TokenType.SymbolSemicolon)
                                            expectedTokens.push(Grammar.Expression)
                                            expectedTokens.push(TokenType.OperatorAssign)
                                            expectedTokens.push(TokenType.BracketSquareClose)
                                            expectedTokens.push(Grammar.Expression)
                                            expectedTokens.push(TokenType.BracketSquareOpen)
                                            expectedTokens.push(TokenType.NameIdentifier)
                                        }

                                        else -> {
                                            // accept?
                                            println("Unexpected token ${ts.nextToken.type}")
                                        }
                                    }
                                }

                                else -> {
                                    println("Unexpected token for Statement ${actual.type}")
                                }
                            }
                        }

                        Grammar.StatementMultiple -> {
                            if (actual.type != TokenType.BracketCurlyClose && actual.type != TokenType.KeyWordReturn) {
                                expectedTokens.push(Grammar.StatementMultiple)
                                expectedTokens.push(Grammar.Statement)
                            }
                        }

                        Grammar.ExpressionList -> {
                            //expression ( ',' expression-list )?
                            if (ts.nextToken.type == TokenType.SymbolComma) {
                                expectedTokens.push(Grammar.ExpressionList)
                                expectedTokens.push(TokenType.SymbolComma)
                            }
                            expectedTokens.push(Grammar.Expression)
                        }

                        Grammar.Expression -> {
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
                                    when (ts.nextToken.type) {
                                        TokenType.KeyWordInt -> {
                                            expectedTokens.push(TokenType.BracketSquareClose)
                                            expectedTokens.push(Grammar.Expression)
                                            expectedTokens.push(TokenType.BracketSquareOpen)
                                            expectedTokens.push(TokenType.KeyWordInt)
                                            expectedTokens.push(TokenType.KeyWordNew)
                                        }

                                        TokenType.NameIdentifier -> {
                                            expectedTokens.push(TokenType.BracketRoundClose)
                                            expectedTokens.push(TokenType.BracketRoundOpen)
                                            expectedTokens.push(TokenType.NameIdentifier)
                                            expectedTokens.push(TokenType.KeyWordNew)
                                        }

                                        else -> println("Unexpected token ${ts.nextToken.type}")
                                    }
                                }
                                //(integer-literal | 'true' | 'false' | identifier | 'this')
                                TokenType.LiteralNumber -> expectedTokens.push(TokenType.LiteralNumber)
                                TokenType.LiteralTrue -> expectedTokens.push(TokenType.LiteralTrue)
                                TokenType.LiteralFalse -> expectedTokens.push(TokenType.LiteralFalse)
                                TokenType.NameIdentifier -> expectedTokens.push(TokenType.NameIdentifier)
                                TokenType.KeyWordThis -> expectedTokens.push(TokenType.KeyWordThis)

                                else -> println("Unexpected token for Expression ${actual.type}")
                            }
                        }

                        Grammar.ExpressionListOptional -> {
                            if (actual.type != TokenType.BracketRoundClose) {
                                expectedTokens.push(Grammar.ExpressionList)
                            }
                        }
                    }
                }

                is TokenType -> {
                    if (expected == actual.type) {
                        ts.accept()
                    } else {
                        println("expected $expected but got ${actual.type}")
                    }
                }
            }
        }
    }
}