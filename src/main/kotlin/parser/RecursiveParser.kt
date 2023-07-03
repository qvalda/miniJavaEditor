package parser

import tokenizer.Token
import tokenizer.TokenType
import kotlin.Exception

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

class ParseException(message: String) : Exception(message)

class ParseError(val lineIndex:Int, val token: Token, val message: String)

class ParserResult(val program: ProgramNode?, val errors:List<ParseError>)

class RecursiveParser(private val ts: ITokenSource) {

    fun parse(): ParserResult {
        return try {
            val parseProgram = parseProgram()
            ParserResult(parseProgram, emptyList())
        } catch (e: ParseException){
            val error = ParseError(ts.lineIndex, ts.currentToken, e.message!!)
            ParserResult(null, listOf(error))
        }
    }

    private fun parseProgram(): ProgramNode {
        //main-class class-declaration*
        val mainClass = parseMainClass()
        val classes = parseClassDeclarationMultiple()
        return ProgramNode(mainClass, classes)
    }

    private fun parseMainClass(): MainClassNode {
        //    'class' identifier '{'
        //        'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{'
        //            var-declaration*
        //            statement*
        //        '}'
        //    '}'
        expected(TokenType.KeyWordClass)
        val name = ts.currentToken.value
        val location = getLocation(ts.currentToken)
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

        return MainClassNode(name, variables, statements, location)
    }

    private fun parseClassDeclarationMultiple(): List<ClassDeclarationNode> {
        val classes = mutableListOf<ClassDeclarationNode>()
        while (!ts.isEOF()) {
            classes.add(parseClassDeclaration())
        }
        return classes
    }

    private fun parseClassDeclaration(): ClassDeclarationNode {
        //    'class' identifier ( 'extends' identifier )? '{'
        //        var-declaration*
        //        method-declaration*
        //    '}'
        expected(TokenType.KeyWordClass)
        val name = ts.currentToken.value
        val location = getLocation(ts.currentToken)
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
        return ClassDeclarationNode(name, baseName, variables, methods, location)
    }

    private fun parseMethodDeclaration(): MethodDeclarationNode {
        //    'public' type identifier '(' formal-list? ')' '{'
        //        var-declaration*
        //        statement*
        //        'return' expression ';'
        //    '}'
        expected(TokenType.KeyWordPublic)
        val type = parseType()
        val name = ts.currentToken.value
        val location = getLocation(ts.currentToken)
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
        return MethodDeclarationNode(name, type, list, variables, statements, returnExp, location)
    }

    private fun parseMethodDeclarationMultiple(): List<MethodDeclarationNode> {
        val methods = mutableListOf<MethodDeclarationNode>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose) {
            methods.add(parseMethodDeclaration())
        }
        return methods
    }

    private fun parseVarDeclaration(): VarDeclarationNode {
        //type identifier ';'
        val type = parseType()
        val name = ts.currentToken.value
        val location = getLocation(ts.currentToken)
        expected(TokenType.NameIdentifier)
        expected(TokenType.SymbolSemicolon)
        return VarDeclarationNode(name, type, location)
    }

    private fun parseMethodVarDeclarationMultiple(): List<VarDeclarationNode> {
        val vars = mutableListOf<VarDeclarationNode>()
        //while (ts.currentToken.type != TokenType.KeyWordReturn && ts.nextToken.type == TokenType.NameIdentifier && ts.nextToken2.type == TokenType.SymbolSemicolon) { //todo: support array
        while (ts.currentToken.type != TokenType.KeyWordReturn && ts.nextToken.type == TokenType.NameIdentifier) { //todo: support array
            vars.add(parseVarDeclaration())
        }
        return vars
    }

    private fun parseClassVarDeclarationMultiple(): List<VarDeclarationNode> {
        val vars = mutableListOf<VarDeclarationNode>()
        while (ts.currentToken.type != TokenType.KeyWordPublic) {
            vars.add(parseVarDeclaration())
        }
        return vars
    }

    private fun parseType(): TypeNodeNode {
        when (ts.currentToken.type) {
            //'boolean'
            TokenType.KeyWordBoolean -> {
                ts.accept()
                return BooleanTypeNode()
            }
            //('int' | 'int' '[' ']')
            TokenType.KeyWordInt -> {
                ts.accept()
                if (ts.currentToken.type == TokenType.BracketSquareOpen) {
                    ts.accept()
                    expected(TokenType.BracketSquareClose)
                    return IntArrayTypeNode()
                } else {
                    return IntTypeNode()
                }
            }
            // identifier
            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                return NameIdentifierTypeNode(name)
            }

            else -> throw ParseException("Unexpected token for Type ${ts.currentToken.type}")
        }
    }

    private fun parseFormalList(): FormalListNode {
        //type identifier ( ',' formal-list )?
        val type = parseType()
        val name = ts.currentToken.value
        expected(TokenType.NameIdentifier)
        return FormalListNode(type, name)
    }

    private fun parseFormalListOptional(): List<FormalListNode> {
        if (ts.currentToken.type == TokenType.BracketRoundClose) {
            return emptyList()
        } else {
            val list = mutableListOf<FormalListNode>()
            list.add(parseFormalList())
            while (ts.currentToken.type == TokenType.SymbolComma) {
                ts.accept()
                list.add(parseFormalList())
            }
            return list
        }
    }

    private fun parseExpressionList(): List<ExpressionNode> {
        val expressions = mutableListOf<ExpressionNode>()
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

    private fun parseExpression(): ExpressionNode {
        val exp = parseSimpleExpression()
        when (ts.currentToken.type) {
            //expression ('&&' | '<' | '+' | '-' | '*') expression
            TokenType.OperatorAnd, TokenType.OperatorLess, TokenType.OperatorPlus, TokenType.OperatorMinus, TokenType.OperatorMult -> {
                val operation = ts.currentToken.type
                ts.accept()
                val b = parseExpression()
                return BinaryExpressionNode(exp, b, operation)
            }
            //expression '[' expression ']'
            TokenType.BracketSquareOpen -> {
                ts.accept()
                val indexer = parseExpression()
                expected(TokenType.BracketSquareClose)
                return IndexerExpressionNode(exp, indexer)
            }
            //expression '.' identifier '(' expression-list? ')'
            //expression '.' 'length'
            TokenType.SymbolDot -> {
                ts.accept()
                val name = ts.currentToken.value
                if (name == "length") {
                    ts.accept()
                    return LengthExpressionNode(exp)
                } else {
                    expected(TokenType.NameIdentifier)
                    expected(TokenType.BracketRoundOpen)
                    val args = parseExpressionList()
                    expected(TokenType.BracketRoundClose)
                    return MethodCallExpressionNode(exp, name, args)
                }
            }

            else -> return exp
        }
    }

    private fun parseSimpleExpression(): ExpressionNode {
        when (ts.currentToken.type) {
            //'(' expression ')'
            TokenType.BracketRoundOpen -> {
                ts.accept()
                val obj = parseExpression()
                expected(TokenType.BracketRoundClose)
                return BracketExpressionNode(obj)
            }
            //'!' expression
            TokenType.OperatorNot -> {
                ts.accept()
                val obj = parseExpression()
                return NotExpressionNode(obj)
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
                        return NewIntArrayExpressionNode(indexer)
                    }

                    TokenType.NameIdentifier -> {
                        val name = ts.currentToken.value
                        ts.accept()
                        expected(TokenType.BracketRoundOpen)
                        expected(TokenType.BracketRoundClose)
                        return NewExpressionNode(name)
                    }

                    else -> throw ParseException("Unexpected token ${ts.currentToken.type}")
                }
            }
            //(integer-literal | 'true' | 'false' | identifier | 'this')
            TokenType.LiteralNumber -> {
                val value = ts.currentToken.value
                ts.accept()
                return LiteralExpressionNode(value)
            }

            TokenType.LiteralTrue -> {
                ts.accept()
                return TrueExpressionNode()
            }

            TokenType.LiteralFalse -> {
                ts.accept()
                return FalseExpressionNode()
            }

            TokenType.NameIdentifier -> {
                val name = ts.currentToken.value
                ts.accept()
                return NameIdentifierExpressionNode(name)
            }

            TokenType.KeyWordThis -> {
                ts.accept()
                return ThisExpressionNode()
            }

            else -> {
                throw ParseException("Unexpected token for Expression ${ts.currentToken.type}")
            }
        }
    }

    private fun parseStatement(): StatementNode {
        when (ts.currentToken.type) {
            //'{' statement* '}'
            TokenType.BracketCurlyOpen -> {
                ts.accept()
                val statements = parseStatementMultiple()
                expected(TokenType.BracketCurlyClose)
                return BlockStatementNode(statements)
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
                return IfElseStatementNode(condition, ifStatement, elseStatement)
            }
            //'while' '(' expression ')' statement
            TokenType.KeyWordWhile -> {
                ts.accept()
                expected(TokenType.BracketRoundOpen)
                val condition = parseExpression()
                expected(TokenType.BracketRoundClose)
                val bodyStatement = parseStatement()
                return WhileStatementNode(condition, bodyStatement)
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
                return PrintStatementNode(exp)
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
                        return AssignStatementNode(name, value)
                    }

                    TokenType.BracketSquareOpen -> {
                        ts.accept()
                        val indexer = parseExpression()
                        expected(TokenType.BracketSquareClose)
                        expected(TokenType.OperatorAssign)
                        val value = parseExpression()
                        expected(TokenType.SymbolSemicolon)
                        return AssignIndexerStatementNode(name, indexer, value)
                    }

                    else -> {
                        throw ParseException("Unexpected token ${ts.currentToken.type}")
                    }
                }
            }

            else -> {
                throw ParseException("Unexpected token for Statement ${ts.currentToken.type}")
            }
        }
    }

    private fun parseStatementMultiple(): List<StatementNode> {
        val statements = mutableListOf<StatementNode>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose && ts.currentToken.type != TokenType.KeyWordReturn) {
            statements.add(parseStatement())
        }
        return statements
    }

    private fun getLocation(token: Token) : NodeLocation{
        return NodeLocation(ts.lineIndex, token.startIndex, token.endIndex)
    }

    private fun expected(tokenType: TokenType): Boolean {
        if (ts.currentToken.type == tokenType) {
            ts.accept()
            return true
        } else {
            throw ParseException("Unexpected token $tokenType but got ${ts.currentToken.type}")
        }
    }
}