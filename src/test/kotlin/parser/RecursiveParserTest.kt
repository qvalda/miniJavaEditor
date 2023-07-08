package parser

import base.BaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tokenizer.TokenType.*
import tokenizer.Tokenizer

class RecursiveParserTest: BaseTest() {

    @ParameterizedTest
    @ValueSource(strings = ["binarysearch.javam", "binarytree.javam", "bubblesort.javam", "factorial.javam", "linearsearch.javam", "linkedlist.javam", "quicksort.javam", "treevisitor.javam"])
    fun canParseTestSamplesWithoutErrors(file: String) {
        val input = getFileContent(file)
        val tokens = Tokenizer().getTokens(input)
        val tokensSource = SignificantTokenSource(ArrayTokensSource(tokens))
        val parserResult = RecursiveParser(tokensSource).parse()

        assert(parserResult.program != null)
        assert(parserResult.errors.isEmpty())
        assert(tokensSource.isEOF())
    }

    @Test
    fun canParseBinaryExpression() {
        val ts = createTokenSource(LiteralNumber, OperatorPlus, NameIdentifier)
        val parserResult = RecursiveParser(ts).parseExpression()
        val exp = parserResult as BinaryExpressionNode
        assertIs<LiteralNumberExpressionNode>(exp.a)
        assertIs<NameIdentifierExpressionNode>(exp.b)
        assertEquals(exp.tokenType, OperatorPlus)
    }

    @Test
    fun canParseMethodCallExpression() {
        val ts = createTokenSource(NameIdentifier, SymbolDot, NameIdentifier, BracketRoundOpen, LiteralNumber, BracketRoundClose)
        val parserResult = RecursiveParser(ts).parseExpression()
        val exp = parserResult as MethodCallExpressionNode
        val obj = exp.obj as NameIdentifierExpressionNode
        val arg = exp.args[0] as LiteralNumberExpressionNode

        assertEquals(obj.name, "0")
        assertEquals(exp.method, "2")
        assertEquals(exp.args.size, 1)
        assertEquals(arg.value, "4")
    }

    @Test
    fun canParseNestedExpression() {
        val ts = createTokenSource(BracketRoundOpen, LiteralNumber, OperatorPlus, BracketRoundOpen, LiteralNumber, OperatorMinus, LiteralNumber, BracketRoundClose, BracketRoundClose)
        val parserResult = RecursiveParser(ts).parseExpression()
        val exp = parserResult as BracketExpressionNode
        val bin = exp.obj as BinaryExpressionNode
        val left = bin.a as LiteralNumberExpressionNode
        val right = (bin.b as BracketExpressionNode).obj as BinaryExpressionNode
        val innerLeft = right.a as LiteralNumberExpressionNode
        val innerRight = right.b as LiteralNumberExpressionNode

        assertEquals(bin.tokenType, OperatorPlus)
        assertEquals(left.value, "1")
        assertEquals(right.tokenType, OperatorMinus)
        assertEquals(innerLeft.value, "4")
        assertEquals(innerRight.value, "6")
    }

    @Test
    fun canParseAssignStatement() {
        val ts = createTokenSource(NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon)
        val parserResult = RecursiveParser(ts).parseStatement()
        val st = parserResult as AssignStatementNode
        val value = st.value as LiteralNumberExpressionNode

        assertEquals(st.name, "0")
        assertEquals(value.value, "2")
    }

    @Test
    fun throwParseAssignStatementWithoutSemicolon() {
        val ts = createTokenSource(NameIdentifier, OperatorAssign, LiteralNumber)
        assertThrows<ParseException> { RecursiveParser(ts).parseStatement() }
    }

    @Test
    fun canParseIfStatement() {
        val ts = createTokenSource(
            KeyWordIf, BracketRoundOpen, LiteralTrue, BracketRoundClose, NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon,
            KeyWordElse, NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon
        )
        val parserResult = RecursiveParser(ts).parseStatement()
        val st = parserResult as IfElseStatementNode

        assertIs<TrueExpressionNode>(st.condition)
        assertIs<AssignStatementNode>(st.ifStatement)
        assertIs<AssignStatementNode>(st.elseStatement)
    }

    @Test
    fun canParseNestedStatement() {
        val ts = createTokenSource(
            BracketCurlyOpen, BracketCurlyOpen, NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon, NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon,
            BracketCurlyClose, NameIdentifier, OperatorAssign, LiteralNumber, SymbolSemicolon, BracketCurlyClose
        )
        val parserResult = RecursiveParser(ts).parseStatement()
        val st = parserResult as BlockStatementNode
        val block = st.statements[0] as BlockStatementNode

        assertIs<AssignStatementNode>(st.statements[1])
        assertEquals(2, st.statements.size)
        assertEquals(2, block.statements.size)
    }

    @Test
    fun canParseFormalList() {
        val ts = createTokenSource(KeyWordInt, NameIdentifier, SymbolComma, NameIdentifier, NameIdentifier)
        val parserResult = RecursiveParser(ts).parseFormalListOptional()

        assertEquals(2, parserResult.size)
        assertIs<IntTypeNode>(parserResult[0].type)
        assertEquals("1", parserResult[0].name)
        assertIs<NameIdentifierTypeNode>(parserResult[1].type)
        assertEquals("4", parserResult[1].name)
    }

    @Test
    fun canParseEmptyClass() {
        val ts = createTokenSource(KeyWordClass, NameIdentifier, BracketCurlyOpen, BracketCurlyClose)
        val parserResult = RecursiveParser(ts).parseClassDeclaration()
        assertEquals("1", parserResult.name)
    }

    @Test
    fun canParseEmptyMethod() {
        val ts = createTokenSource(KeyWordPublic, KeyWordInt, NameIdentifier, BracketRoundOpen, BracketRoundClose,  BracketCurlyOpen, KeyWordReturn, LiteralNumber, SymbolSemicolon, BracketCurlyClose)
        val parserResult = RecursiveParser(ts).parseMethodDeclaration()
        assertEquals("2", parserResult.name)
    }

    @Test
    fun provideParseErrors() {
        val ts = createTokenSource(KeyWordClass, NameIdentifier, BracketRoundOpen)
        val parserResult = RecursiveParser(ts).parse()
        assertNull(parserResult.program)
        assertEquals(1, parserResult.errors.size)
        assertEquals(BracketRoundOpen, parserResult.errors[0].token.type)
        assertEquals("Expected 'BracketCurlyOpen', but got '[0:0]BracketRoundOpen=2'", parserResult.errors[0].message)
    }
}