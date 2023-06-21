package parser

import TokensSource
import tokenizer.TokenType
import java.lang.Exception

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

class RecursiveParser {
    fun parse(ts: TokensSource, entryPoint: Grammar = Grammar.Program): Program {
        return parseProgram(ts)
    }

    fun parseProgram(ts: TokensSource): Program {
        //main-class class-declaration*
        val mainClass = parseMainClass(ts)
        val classes = parseClassDeclarationMultiple(ts)
        return Program(mainClass, classes)
    }

    fun parseMainClass(ts: TokensSource): MainClass {
        //    'class' identifier '{'
        //        'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
        //            var-declaration*
        //            statement*
        //        '}'
        //    '}'
        expected(TokenType.KeyWordClass, ts)
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.BracketCurlyOpen, ts)
        expected(TokenType.KeyWordPublic, ts)
        expected(TokenType.KeyWordStatic, ts)
        expected(TokenType.KeyWordVoid, ts)
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.BracketRoundOpen, ts)
        expected(TokenType.KeyWordString, ts)
        expected(TokenType.BracketSquareOpen, ts)
        expected(TokenType.BracketSquareClose, ts)
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.BracketRoundClose, ts)
        expected(TokenType.BracketCurlyOpen, ts)
        val variables = parseVarDeclarationMultiple(ts)
        val statements = parseStatementMultiple(ts)
        expected(TokenType.BracketCurlyClose, ts)
        expected(TokenType.BracketCurlyClose, ts)

        return MainClass(name, variables, statements)
    }

    fun parseClassDeclarationMultiple(ts: TokensSource): Array<ClassDeclaration> {
        val classes = ArrayList<ClassDeclaration>()
        while (ts.currentToken.type != TokenType.EOF) {
            classes.add(parseClassDeclaration(ts))
        }
        return classes.toTypedArray()
    }

    fun parseClassDeclaration(ts: TokensSource): ClassDeclaration {
        //    'class' identifier ( 'extends' identifier )? '{'
        //        var-declaration*
        //        method-declaration*
        //    '}'
        expected(TokenType.KeyWordClass, ts) //todo support 'extends'
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.BracketCurlyOpen, ts)
        val variables = parseVarDeclarationMultiple(ts)
        val methods = parseMethodDeclarationMultiple(ts)
        expected(TokenType.BracketCurlyClose, ts)
        return ClassDeclaration(name, variables, methods)
    }

    fun parseMethodDeclaration(ts: TokensSource): MethodDeclaration {
        //    'public' type identifier '(' formal-list? ')' '{'
        //        var-declaration*
        //        statement*
        //        'return' expression ';'
        //    '}'
        expected(TokenType.KeyWordPublic, ts)
        val type = parseType(ts)
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.BracketRoundOpen, ts)
        val list = parseFormalListOptional(ts)
        expected(TokenType.BracketRoundClose, ts)
        expected(TokenType.BracketCurlyOpen, ts)
        val variables = parseVarDeclarationMultiple(ts)
        val statements = parseStatementMultiple(ts)
        expected(TokenType.KeyWordReturn, ts)
        val returnExp = parseExpression(ts)
        expected(TokenType.SymbolSemicolon, ts)
        expected(TokenType.BracketCurlyClose, ts)
        return MethodDeclaration(name, type, list, variables, statements, returnExp)
    }

    fun parseMethodDeclarationMultiple(ts: TokensSource): Array<MethodDeclaration> {
        val methods = ArrayList<MethodDeclaration>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose) {
            methods.add(parseMethodDeclaration(ts))
        }
        return methods.toTypedArray()
    }

    fun parseVarDeclaration(ts: TokensSource): VarDeclaration {
        //type identifier ';'
        val type = parseType(ts)
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier, ts)
        expected(TokenType.SymbolSemicolon, ts)
        return VarDeclaration(name, type)
    }

    fun parseVarDeclarationMultiple(ts: TokensSource): Array<VarDeclaration> {
        val vars = ArrayList<VarDeclaration>()
        while (ts.nextToken.type == TokenType.NameIdentifier && ts.nextToken2.type == TokenType.SymbolSemicolon) { //todo: support array
            vars.add(parseVarDeclaration(ts))
        }
        return vars.toTypedArray()
    }

    fun parseType(ts: TokensSource): Type {
        when (ts.currentToken.type) {
            //'boolean'
            TokenType.KeyWordBoolean -> {
                ts.accept()
                return BooleanType()
            }
            //('int' | 'int' '[' ']')
            TokenType.KeyWordInt -> {
                ts.accept()
                if (ts.currentToken.type == TokenType.BracketSquareOpen) {
                    ts.accept()
                    expected(TokenType.BracketSquareOpen, ts)
                    return IntArrayType()
                } else {
                    return IntType()
                }
            }
            // identifier
            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                return NameIdentifierType(name)
            }

            else -> println("Unexpected token for Type ${ts.currentToken.type}")
        }
        throw Exception("unexpected parseType")
    }

    fun parseFormalList(ts: TokensSource): FormalList {
        //type identifier ( ',' formal-list )?
        val type = parseType(ts)
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier, ts)
        return FormalList(type, name)
    }

    fun parseFormalListOptional(ts: TokensSource): Array<FormalList> {
        if (ts.currentToken.type == TokenType.BracketRoundClose) {
            return emptyArray()
        } else {
            val list = ArrayList<FormalList>()
            list.add(parseFormalList(ts))
            while (ts.currentToken.type == TokenType.SymbolComma) {
                ts.accept()
                list.add(parseFormalList(ts))
            }
            return list.toTypedArray()
        }
    }

    fun parseExpressionList(ts: TokensSource): Array<Expression> {
        val exprs = ArrayList<Expression>()
        if (ts.currentToken.type != TokenType.BracketRoundClose) {
            while (true) {
                exprs.add(parseExpression(ts))
                if (ts.currentToken.type == TokenType.SymbolComma) {
                    ts.accept()
                    continue
                } else {
                    break
                }
            }
        }
        return exprs.toTypedArray()
    }

    fun parseExpression(ts: TokensSource, lookForward: Boolean = true): Expression {
        if (lookForward) {
            when (ts.nextToken.type) {
                //expression ('&&' | '<' | '+' | '-' | '*') expression
                TokenType.OperatorAnd, TokenType.OperatorLess, TokenType.OperatorPlus, TokenType.OperatorMinus, TokenType.OperatorMult -> {
                    val operation = ts.nextToken.type
                    val a = parseExpression(ts, false)
                    expected(operation, ts)
                    val b = parseExpression(ts)
                    return BinaryExpression(a, b, operation)
                }
                //expression '[' expression ']'
                TokenType.BracketSquareOpen -> {
                    val obj = parseExpression(ts, false)
                    expected(TokenType.BracketSquareOpen, ts)
                    val indexer = parseExpression(ts)
                    expected(TokenType.BracketSquareClose, ts)
                    return IndexerExpression(obj, indexer)
                }
                //expression '.' identifier '(' expression-list? ')'
                TokenType.SymbolDot -> {
                    val obj = parseExpression(ts, false)
                    expected(TokenType.SymbolDot, ts)
                    val name = ts.currentToken.value
                    expected(TokenType.NameIdentifier, ts)
                    expected(TokenType.BracketRoundOpen, ts)
                    val args = parseExpressionList(ts)
                    expected(TokenType.BracketRoundClose, ts)
                    return MethodCallExpression(obj, name, args)
                }

                else -> {} // just fine
            }
        }
        when (ts.currentToken.type) {
            //'(' expression ')'
            TokenType.BracketRoundOpen -> {
                ts.accept()
                val obj = parseExpression(ts)
                expected(TokenType.BracketRoundClose, ts)
                return BracketExpression(obj)
            }
            //'!' expression
            TokenType.OperatorNot -> {
                ts.accept()
                val obj = parseExpression(ts)
                return NotExpression(obj)
            }
            //'new' 'int' '[' expression ']'
            //'new' identifier '(' ')'
            TokenType.KeyWordNew -> {
                ts.accept()
                when (ts.currentToken.type) {
                    TokenType.KeyWordInt -> {
                        ts.accept()
                        expected(TokenType.BracketSquareOpen, ts)
                        val indexer = parseExpression(ts)
                        expected(TokenType.BracketSquareClose, ts)
                        return NewIntArrayExpression(indexer)
                    }

                    TokenType.NameIdentifier -> {
                        val name = ts.currentToken.value
                        ts.accept()
                        expected(TokenType.BracketRoundOpen, ts)
                        expected(TokenType.BracketRoundClose, ts)
                        return NewExpression(name)
                    }

                    else -> println("Unexpected token ${ts.currentToken.type}")
                }
            }
            //(integer-literal | 'true' | 'false' | identifier | 'this')
            TokenType.LiteralNumber -> {
                val value = ts.currentToken.value
                ts.accept()
                return LiteralExpression(value)
            }

            TokenType.LiteralTrue -> {
                ts.accept()
                return TrueExpression()
            }

            TokenType.LiteralFalse -> {
                ts.accept()
                return FalseExpression()
            }

            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                return NameIdentifierExpression(name)
            }

            TokenType.KeyWordThis -> {
                ts.accept()
                return ThisExpression()
            }

            else -> println("Unexpected token for Expression ${ts.currentToken.type}")
        }
        throw Exception("unepected")
    }

    fun parseStatement(ts: TokensSource): Statement {
        when (ts.currentToken.type) {
            //'{' statement* '}'
            TokenType.BracketCurlyOpen -> {
                ts.accept()
                val statements = parseStatementMultiple(ts)
                expected(TokenType.BracketCurlyClose, ts)
                return BlockStatement(statements)
            }
            //'if' '(' expression ')' statement 'else' statement
            TokenType.KeyWordIf -> {
                ts.accept()
                expected(TokenType.BracketRoundOpen, ts)
                val condition = parseExpression(ts)
                expected(TokenType.BracketRoundClose, ts)
                val ifStatement = parseStatement(ts)
                expected(TokenType.KeyWordElse, ts)
                val elseStatement = parseStatement(ts)
                return IfElseStatement(condition, ifStatement, elseStatement)
            }
            //'while' '(' expression ')' statement
            TokenType.KeyWordWhile -> {
                ts.accept()
                expected(TokenType.BracketRoundOpen, ts)
                val condition = parseExpression(ts)
                expected(TokenType.BracketRoundClose, ts)
                val bodyStatement = parseStatement(ts)
                return WhileStatement(condition, bodyStatement)
            }
            //identifier '=' expression ';'
            //identifier '[' expression ']' '=' expression ';'
            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                when (ts.currentToken.type) {
                    TokenType.OperatorAssign -> {
                        ts.accept()
                        val value = parseExpression(ts)
                        expected(TokenType.SymbolSemicolon, ts)
                        return AssignStatement(name, value)
                    }

                    TokenType.BracketSquareOpen -> {
                        ts.accept()
                        val indexer = parseExpression(ts)
                        expected(TokenType.BracketSquareClose, ts)
                        expected(TokenType.OperatorAssign, ts)
                        val value = parseExpression(ts)
                        expected(TokenType.SymbolSemicolon, ts)
                        return AssignIndexerStatement(name, indexer, value)
                    }

                    else -> {
                        // accept?
                        println("Unexpected token ${ts.currentToken.type}")
                    }
                }
            }

            else -> {
                println("Unexpected token for Statement ${ts.currentToken.type}")
            }
        }
        throw Exception("unexpected")
    }

    fun parseStatementMultiple(ts: TokensSource): Array<Statement> {
        val statements = ArrayList<Statement>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose && ts.currentToken.type != TokenType.KeyWordReturn) {
            statements.add(parseStatement(ts))
        }
        return statements.toTypedArray()
    }

    fun expected(tokenType: TokenType, ts: TokensSource): Boolean {
        if (ts.currentToken.type == tokenType) {
            ts.accept()
            return true
        } else {
            println("Unexpected token $tokenType but got ${ts.currentToken.type}")
            return false
        }
    }
}