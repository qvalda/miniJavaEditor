package parser

import tokenizer.Token
import tokenizer.TokenType

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
             | (integer-literal | 'true' | 'false' | identifier | 'this' | string-literal | char-literal)
             | 'new' 'int' '[' expression ']'
             | 'new' identifier '(' ')'
             | '!' expression
             | '(' expression ')'
expression-list ::= expression ( ',' expression-list )?
 */

class RecursiveParser(private val ts: ITokenSource) {

    fun parse(): ParserResult {
        return try {
            val parseProgram = parseProgram()
            ParserResult(parseProgram, emptyList())
        } catch (e: ParseException) {
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
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
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
        val varAndStatements = parseMethodVarDeclarationOrStatementMultiple()
        val variables = varAndStatements.first
        val statements = varAndStatements.second + parseStatementMultiple()
        expected(TokenType.BracketCurlyClose)
        expected(TokenType.BracketCurlyClose)

        return MainClassNode(nameToken.value!!, variables, statements, location)
    }

    private fun parseClassDeclarationMultiple(): List<ClassDeclarationNode> {
        val classes = mutableListOf<ClassDeclarationNode>()
        while (!ts.isEOF()) {
            classes.add(parseClassDeclaration())
        }
        return classes
    }

    fun parseClassDeclaration(): ClassDeclarationNode {
        //    'class' identifier ( 'extends' identifier )? '{'
        //        var-declaration*
        //        method-declaration*
        //    '}'
        expected(TokenType.KeyWordClass)
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
        var baseNameToken: Token? = null
        if (ts.currentToken.type == TokenType.KeyWordExtends) {
            expected(TokenType.KeyWordExtends)
            baseNameToken = expected(TokenType.NameIdentifier)
        }
        expected(TokenType.BracketCurlyOpen)
        val variables = parseClassVarDeclarationMultiple()
        val methods = parseMethodDeclarationMultiple()
        expected(TokenType.BracketCurlyClose)
        return ClassDeclarationNode(nameToken.value!!, baseNameToken?.value, variables, methods, location)
    }

    fun parseMethodDeclaration(): MethodDeclarationNode {
        //    'public' type identifier '(' formal-list? ')' '{'
        //        var-declaration*
        //        statement*
        //        'return' expression ';'
        //    '}'
        expected(TokenType.KeyWordPublic)
        val type = parseType()
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
        expected(TokenType.BracketRoundOpen)
        val list = parseFormalListOptional()
        expected(TokenType.BracketRoundClose)
        expected(TokenType.BracketCurlyOpen)
        val varAndStatements = parseMethodVarDeclarationOrStatementMultiple()
        val variables = varAndStatements.first
        val statements = varAndStatements.second + parseStatementMultiple()
        expected(TokenType.KeyWordReturn)
        val returnExp = parseExpression()
        expected(TokenType.SymbolSemicolon)
        expected(TokenType.BracketCurlyClose)
        return MethodDeclarationNode(nameToken.value!!, type, list, variables, statements, returnExp, location)
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
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
        expected(TokenType.SymbolSemicolon)
        return VarDeclarationNode(nameToken.value!!, type, location)
    }

    private fun parseVarDeclaration(type: TypeNode): VarDeclarationNode {
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
        expected(TokenType.SymbolSemicolon)
        return VarDeclarationNode(nameToken.value!!, type, location)
    }

    private fun parseMethodVarDeclarationOrStatementMultiple(): Pair<List<VarDeclarationNode>, List<StatementNode>> {
        val vars = mutableListOf<VarDeclarationNode>()
        val statements = mutableListOf<StatementNode>()
        while (true) {
            if (ts.currentToken.type in arrayOf(TokenType.KeyWordInt, TokenType.KeyWordBoolean, TokenType.KeyWordString, TokenType.KeyWordChar)) {
                vars.add(parseVarDeclaration())
            } else if (ts.currentToken.type == TokenType.NameIdentifier) {
                val nameToken = expected(TokenType.NameIdentifier)
                if (ts.currentToken.type == TokenType.NameIdentifier) {
                    vars.add(parseVarDeclaration(NameIdentifierTypeNode(nameToken.value)))
                } else {
                    parseStatementAfterIdentifier(nameToken.value)
                    return Pair(vars, statements)
                }
            } else {
                return Pair(vars, statements)
            }
        }
    }

    private fun parseClassVarDeclarationMultiple(): List<VarDeclarationNode> {
        val vars = mutableListOf<VarDeclarationNode>()
        while (ts.currentToken.type != TokenType.KeyWordPublic && ts.currentToken.type != TokenType.BracketCurlyClose) {
            vars.add(parseVarDeclaration())
        }
        return vars
    }

    private fun parseType(): TypeNode {
        when (ts.currentToken.type) {
            //'boolean'
            TokenType.KeyWordBoolean -> {
                expected(TokenType.KeyWordBoolean)
                return BooleanTypeNode()
            }
            // String
            TokenType.KeyWordString -> {
                expected(TokenType.KeyWordString)
                return StringTypeNode()
            }
            // char
            TokenType.KeyWordChar -> {
                expected(TokenType.KeyWordChar)
                return CharTypeNode()
            }
            //('int' | 'int' '[' ']')
            TokenType.KeyWordInt -> {
                expected(TokenType.KeyWordInt)
                if (ts.currentToken.type == TokenType.BracketSquareOpen) {
                    expected(TokenType.BracketSquareOpen)
                    expected(TokenType.BracketSquareClose)
                    return IntArrayTypeNode()
                } else {
                    return IntTypeNode()
                }
            }
            // identifier
            TokenType.NameIdentifier -> {
                val nameToken = expected(TokenType.NameIdentifier)
                return NameIdentifierTypeNode(nameToken.value)
            }

            else -> throwUnexpectedException("Type statement")
        }
    }

    fun parseFormalListOptional(): List<FormalListNode> {
        if (ts.currentToken.type == TokenType.BracketRoundClose) {
            return emptyList()
        } else {
            val list = mutableListOf<FormalListNode>()
            list.add(parseFormalList())
            while (ts.currentToken.type == TokenType.SymbolComma) {
                expected(TokenType.SymbolComma)
                list.add(parseFormalList())
            }
            return list
        }
    }

    private fun parseFormalList(): FormalListNode {
        //type identifier ( ',' formal-list )?
        val type = parseType()
        val (nameToken, location) = expectedWithLocation(TokenType.NameIdentifier)
        return FormalListNode(type, nameToken.value!!, location)
    }

    private fun parseExpressionList(): List<ExpressionNode> {
        val expressions = mutableListOf<ExpressionNode>()
        if (ts.currentToken.type != TokenType.BracketRoundClose) {
            while (true) {
                expressions.add(parseExpression())
                if (ts.currentToken.type == TokenType.SymbolComma) {
                    expected(TokenType.SymbolComma)
                    continue
                } else {
                    break
                }
            }
        }
        return expressions
    }

    fun parseExpression(): ExpressionNode {
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
                expected(TokenType.BracketSquareOpen)
                val indexer = parseExpression()
                expected(TokenType.BracketSquareClose)
                return IndexerExpressionNode(exp, indexer)
            }
            //expression '.' identifier '(' expression-list? ')'
            //expression '.' 'length'
            TokenType.SymbolDot -> {
                expected(TokenType.SymbolDot)
                val nameToken = expected(TokenType.NameIdentifier)
                if (nameToken.value == "length") {
                    return LengthExpressionNode(exp)
                } else {
                    expected(TokenType.BracketRoundOpen)
                    val args = parseExpressionList()
                    expected(TokenType.BracketRoundClose)
                    return MethodCallExpressionNode(exp, nameToken.value, args)
                }
            }

            else -> return exp
        }
    }

    private fun parseSimpleExpression(): ExpressionNode {
        when (ts.currentToken.type) {
            //'(' expression ')'
            TokenType.BracketRoundOpen -> {
                expected(TokenType.BracketRoundOpen)
                val obj = parseExpression()
                expected(TokenType.BracketRoundClose)
                return BracketExpressionNode(obj)
            }
            //'!' expression
            TokenType.OperatorNot -> {
                expected(TokenType.OperatorNot)
                val obj = parseExpression()
                return NotExpressionNode(obj)
            }
            //'new' 'int' '[' expression ']'
            //'new' identifier '(' ')'
            TokenType.KeyWordNew -> {
                expected(TokenType.KeyWordNew)
                when (ts.currentToken.type) {
                    TokenType.KeyWordInt -> {
                        expected(TokenType.KeyWordInt)
                        expected(TokenType.BracketSquareOpen)
                        val indexer = parseExpression()
                        expected(TokenType.BracketSquareClose)
                        return NewIntArrayExpressionNode(indexer)
                    }

                    TokenType.NameIdentifier -> {
                        val nameToken = expected(TokenType.NameIdentifier)
                        expected(TokenType.BracketRoundOpen)
                        expected(TokenType.BracketRoundClose)
                        return NewExpressionNode(nameToken.value)
                    }

                    else -> throwUnexpectedException("Expression statement")
                }
            }
            //(integer-literal | 'true' | 'false' | identifier | 'this' | string-literal | char-literal)
            TokenType.LiteralNumber -> {
                val nameToken = expected(TokenType.LiteralNumber)
                return LiteralNumberExpressionNode(nameToken.value)
            }

            TokenType.LiteralTrue -> {
                expected(TokenType.LiteralTrue)
                return TrueExpressionNode()
            }

            TokenType.LiteralFalse -> {
                expected(TokenType.LiteralFalse)
                return FalseExpressionNode()
            }

            TokenType.NameIdentifier -> {
                val nameToken = expected(TokenType.NameIdentifier)
                return NameIdentifierExpressionNode(nameToken.value)
            }

            TokenType.KeyWordThis -> {
                expected(TokenType.KeyWordThis)
                return ThisExpressionNode()
            }

            TokenType.LiteralString -> {
                val nameToken = expected(TokenType.LiteralString)
                return LiteralStringExpressionNode(nameToken.value)
            }

            TokenType.LiteralChar -> {
                val nameToken = expected(TokenType.LiteralChar)
                return LiteralCharExpressionNode(nameToken.value)
            }

            else -> throwUnexpectedException("Expression statement")
        }
    }

    fun parseStatement(): StatementNode {
        when (ts.currentToken.type) {
            //'{' statement* '}'
            TokenType.BracketCurlyOpen -> {
                expected(TokenType.BracketCurlyOpen)
                val statements = parseStatementMultiple()
                expected(TokenType.BracketCurlyClose)
                return BlockStatementNode(statements)
            }
            //'if' '(' expression ')' statement 'else' statement
            TokenType.KeyWordIf -> {
                expected(TokenType.KeyWordIf)
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
                expected(TokenType.KeyWordWhile)
                expected(TokenType.BracketRoundOpen)
                val condition = parseExpression()
                expected(TokenType.BracketRoundClose)
                val bodyStatement = parseStatement()
                return WhileStatementNode(condition, bodyStatement)
            }
            //'System''.''out''.''println' '(' expression ')' ';'
            TokenType.KeyWordSystem -> {
                expected(TokenType.KeyWordSystem)
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
                val nameToken = expected(TokenType.NameIdentifier)
                return parseStatementAfterIdentifier(nameToken.value)
            }

            else -> throwUnexpectedException("Statement")
        }
    }

    private fun parseStatementMultiple(): List<StatementNode> {
        val statements = mutableListOf<StatementNode>()
        while (ts.currentToken.type != TokenType.BracketCurlyClose && ts.currentToken.type != TokenType.KeyWordReturn) {
            statements.add(parseStatement())
        }
        return statements
    }

    private fun parseStatementAfterIdentifier(name: String?): StatementNode {
        when (ts.currentToken.type) {
            TokenType.OperatorAssign -> {
                expected(TokenType.OperatorAssign)
                val value = parseExpression()
                expected(TokenType.SymbolSemicolon)
                return AssignStatementNode(name, value)
            }

            TokenType.BracketSquareOpen -> {
                expected(TokenType.BracketSquareOpen)
                val indexer = parseExpression()
                expected(TokenType.BracketSquareClose)
                expected(TokenType.OperatorAssign)
                val value = parseExpression()
                expected(TokenType.SymbolSemicolon)
                return AssignIndexerStatementNode(name, indexer, value)
            }

            else -> throwUnexpectedException("Statement")
        }
    }

    private fun expected(tokenType: TokenType): Token {
        if (ts.currentToken.type == tokenType) {
            val result = ts.currentToken
            ts.accept()
            return result
        }
        throwUnexpectedException(tokenType.toString())
    }

    private fun expectedWithLocation(tokenType: TokenType): Pair<Token, NodeLocation> {
        if (ts.currentToken.type == tokenType) {
            val result = ts.currentToken
            val location = NodeLocation(ts.lineIndex, result.startIndex, result.endIndex)
            ts.accept()
            return Pair(result, location)
        }
        throwUnexpectedException(tokenType.toString())
    }

    private fun throwUnexpectedException(expected: String): Nothing {
        throw ParseException("Expected '${expected}', but got '${ts.currentToken}'")
    }
}