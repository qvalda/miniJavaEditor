package parser

import tokenizer.ITokenSource
import tokenizer.TokensSource
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

class RecursiveParser(private val ts: ITokenSource) {

    fun parse(entryPoint: Grammar = Grammar.Program): Program {
        return parseProgram()
    }

    private fun parseProgram(): Program {
        //main-class class-declaration*
        val mainClass = parseMainClass()
        val classes = parseClassDeclarationMultiple()
        return Program(mainClass, classes)
    }

    private fun parseMainClass(): MainClass {
        //    'class' identifier '{'
        //        'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
        //            var-declaration*
        //            statement*
        //        '}'
        //    '}'
        expected(TokenType.KeyWordClass)
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier)
        expected(TokenType.BracketCurlyOpen)
        expected(TokenType.KeyWordPublic)
        expected(TokenType.KeyWordStatic)
        expected(TokenType.KeyWordVoid)
        expected(TokenType.NameIdentifier)
        expected(TokenType.BracketRoundOpen)
        expected(TokenType.KeyWordString)
        expected(TokenType.BracketSquareOpen)
        expected(TokenType.BracketSquareClose)
        expected(TokenType.NameIdentifier)
        expected(TokenType.BracketRoundClose)
        expected(TokenType.BracketCurlyOpen)
        val variables = parseMethodVarDeclarationMultiple()
        val statements = parseStatementMultiple()
        expected(TokenType.BracketCurlyClose)
        expected(TokenType.BracketCurlyClose)

        return MainClass(name, variables, statements)
    }

    private fun parseClassDeclarationMultiple(): List<ClassDeclaration> {
        val classes = mutableListOf<ClassDeclaration>()
        while (!ts.isEOF()) {
            classes.add(parseClassDeclaration())
        }
        return classes
    }

    private fun parseClassDeclaration(): ClassDeclaration {
        //    'class' identifier ( 'extends' identifier )? '{'
        //        var-declaration*
        //        method-declaration*
        //    '}'
        expected(TokenType.KeyWordClass)
        val name = ts.currentToken.value
        var baseName: String? = null
        expected(TokenType.NameIdentifier)
        if (ts.currentToken.type == TokenType.KeyWordExtends) {
            ts.accept()
            baseName = ts.currentToken.value
            expected(TokenType.NameIdentifier)
        }
        expected(TokenType.BracketCurlyOpen)
        val variables = parseClassVarDeclarationMultiple()
        val methods = parseMethodDeclarationMultiple()
        expected(TokenType.BracketCurlyClose)
        return ClassDeclaration(name, baseName, variables, methods)
    }

    private fun parseMethodDeclaration(): MethodDeclaration {
        //    'public' type identifier '(' formal-list? ')' '{'
        //        var-declaration*
        //        statement*
        //        'return' expression ';'
        //    '}'
        expected(TokenType.KeyWordPublic)
        val type = parseType()
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier)
        expected(TokenType.BracketRoundOpen)
        val list = parseFormalListOptional()
        expected(TokenType.BracketRoundClose)
        expected(TokenType.BracketCurlyOpen)
        val variables = parseMethodVarDeclarationMultiple()
        val statements = parseStatementMultiple()
        expected(TokenType.KeyWordReturn)
        val returnExp = parseExpression()
        expected(TokenType.SymbolSemicolon)
        expected(TokenType.BracketCurlyClose)
        return MethodDeclaration(name, type, list, variables, statements, returnExp)
    }

    private fun parseMethodDeclarationMultiple(): List<MethodDeclaration> {
        val methods = mutableListOf<MethodDeclaration>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose) {
            methods.add(parseMethodDeclaration())
        }
        return methods
    }

    private fun parseVarDeclaration(): VarDeclaration {
        //type identifier ';'
        val type = parseType()
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier)
        expected(TokenType.SymbolSemicolon)
        return VarDeclaration(name, type)
    }

    private fun parseMethodVarDeclarationMultiple(): List<VarDeclaration> {
        val vars = mutableListOf<VarDeclaration>()
        //while (ts.currentToken.type != TokenType.KeyWordReturn && ts.nextToken.type == TokenType.NameIdentifier && ts.nextToken2.type == TokenType.SymbolSemicolon) { //todo: support array
        while (ts.currentToken.type != TokenType.KeyWordReturn && ts.nextToken.type == TokenType.NameIdentifier) { //todo: support array
            vars.add(parseVarDeclaration())
        }
        return vars
    }

    private fun parseClassVarDeclarationMultiple(): List<VarDeclaration> {
        val vars = mutableListOf<VarDeclaration>()
        while (ts.currentToken.type != TokenType.KeyWordPublic) {
            vars.add(parseVarDeclaration())
        }
        return vars
    }

    private fun parseType(): Type {
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
                    expected(TokenType.BracketSquareClose)
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

    private fun parseFormalList(): FormalList {
        //type identifier ( ',' formal-list )?
        val type = parseType()
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier)
        return FormalList(type, name)
    }

    private fun parseFormalListOptional(): List<FormalList> {
        if (ts.currentToken.type == TokenType.BracketRoundClose) {
            return emptyList()
        } else {
            val list = mutableListOf<FormalList>()
            list.add(parseFormalList())
            while (ts.currentToken.type == TokenType.SymbolComma) {
                ts.accept()
                list.add(parseFormalList())
            }
            return list
        }
    }

    private fun parseExpressionList(): List<Expression> {
        val expressions = mutableListOf<Expression>()
        if (ts.currentToken.type != TokenType.BracketRoundClose) {
            while (true) {
                expressions.add(parseExpression())
                if (ts.currentToken.type == TokenType.SymbolComma) {
                    ts.accept()
                    continue
                } else {
                    break
                }
            }
        }
        return expressions
    }

    private fun parseExpression(): Expression {
        val exp = parseSimpleExpression()
        when (ts.currentToken.type) {
            //expression ('&&' | '<' | '+' | '-' | '*') expression
            TokenType.OperatorAnd, TokenType.OperatorLess, TokenType.OperatorPlus, TokenType.OperatorMinus, TokenType.OperatorMult -> {
                val operation = ts.currentToken.type
                ts.accept()
                val b = parseExpression()
                return BinaryExpression(exp, b, operation)
            }
            //expression '[' expression ']'
            TokenType.BracketSquareOpen -> {
                ts.accept()
                val indexer = parseExpression()
                expected(TokenType.BracketSquareClose)
                return IndexerExpression(exp, indexer)
            }
            //expression '.' identifier '(' expression-list? ')'
            //expression '.' 'length'
            TokenType.SymbolDot -> {
                ts.accept()
                val name = ts.currentToken.value
                if (name == "length") {
                    ts.accept()
                    return LengthExpression(exp)
                } else {
                    expected(TokenType.NameIdentifier)
                    expected(TokenType.BracketRoundOpen)
                    val args = parseExpressionList()
                    expected(TokenType.BracketRoundClose)
                    return MethodCallExpression(exp, name, args)
                }
            }

            else -> return exp
        }
    }

    private fun parseSimpleExpression(): Expression {
        when (ts.currentToken.type) {
            //'(' expression ')'
            TokenType.BracketRoundOpen -> {
                ts.accept()
                val obj = parseExpression()
                expected(TokenType.BracketRoundClose)
                return BracketExpression(obj)
            }
            //'!' expression
            TokenType.OperatorNot -> {
                ts.accept()
                val obj = parseExpression()
                return NotExpression(obj)
            }
            //'new' 'int' '[' expression ']'
            //'new' identifier '(' ')'
            TokenType.KeyWordNew -> {
                ts.accept()
                when (ts.currentToken.type) {
                    TokenType.KeyWordInt -> {
                        ts.accept()
                        expected(TokenType.BracketSquareOpen)
                        val indexer = parseExpression()
                        expected(TokenType.BracketSquareClose)
                        return NewIntArrayExpression(indexer)
                    }

                    TokenType.NameIdentifier -> {
                        val name = ts.currentToken.value
                        ts.accept()
                        expected(TokenType.BracketRoundOpen)
                        expected(TokenType.BracketRoundClose)
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

            else -> {
                println("Unexpected token for Expression ${ts.currentToken.type}")
            }
        }
        throw Exception("unexpected")
    }

    private fun parseStatement(): Statement {
        when (ts.currentToken.type) {
            //'{' statement* '}'
            TokenType.BracketCurlyOpen -> {
                ts.accept()
                val statements = parseStatementMultiple()
                expected(TokenType.BracketCurlyClose)
                return BlockStatement(statements)
            }
            //'if' '(' expression ')' statement 'else' statement
            TokenType.KeyWordIf -> {
                ts.accept()
                expected(TokenType.BracketRoundOpen)
                val condition = parseExpression()
                expected(TokenType.BracketRoundClose)
                val ifStatement = parseStatement()
                expected(TokenType.KeyWordElse)
                val elseStatement = parseStatement()
                return IfElseStatement(condition, ifStatement, elseStatement)
            }
            //'while' '(' expression ')' statement
            TokenType.KeyWordWhile -> {
                ts.accept()
                expected(TokenType.BracketRoundOpen)
                val condition = parseExpression()
                expected(TokenType.BracketRoundClose)
                val bodyStatement = parseStatement()
                return WhileStatement(condition, bodyStatement)
            }
            //'System''.''out''.''println' '(' expression ')' ';'
            TokenType.KeyWordSystem -> {
                ts.accept()
                expected(TokenType.SymbolDot)
                expected(TokenType.KeyWordOut)
                expected(TokenType.SymbolDot)
                expected(TokenType.KeyWordPrintln)
                expected(TokenType.BracketRoundOpen)
                val exp = parseExpression()
                expected(TokenType.BracketRoundClose)
                expected(TokenType.SymbolSemicolon)
                return PrintStatement(exp)
            }
            //identifier '=' expression ';'
            //identifier '[' expression ']' '=' expression ';'
            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                when (ts.currentToken.type) {
                    TokenType.OperatorAssign -> {
                        ts.accept()
                        val value = parseExpression()
                        expected(TokenType.SymbolSemicolon)
                        return AssignStatement(name, value)
                    }

                    TokenType.BracketSquareOpen -> {
                        ts.accept()
                        val indexer = parseExpression()
                        expected(TokenType.BracketSquareClose)
                        expected(TokenType.OperatorAssign)
                        val value = parseExpression()
                        expected(TokenType.SymbolSemicolon)
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

    private fun parseStatementMultiple(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose && ts.currentToken.type != TokenType.KeyWordReturn) {
            statements.add(parseStatement())
        }
        return statements
    }

    private fun expected(tokenType: TokenType): Boolean {
        if (ts.currentToken.type == tokenType) {
            ts.accept()
            return true
        } else {
            println("Unexpected token $tokenType but got ${ts.currentToken.type}")
            return false
        }
    }
}