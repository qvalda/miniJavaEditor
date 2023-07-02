package parser

import tokenizer.TokenType

open class Node

class Program(val mainClass: MainClass, val classes : List<ClassDeclaration>) : Node()

class MainClass(val name: String?,val  variables: List<VarDeclaration>,val  statements: List<Statement>): Node()

class ClassDeclaration(val name:String?, val baseName:String?, val variables : List<VarDeclaration>,val  methods : List<MethodDeclaration>): Node()

class VarDeclaration(val name: String?, val type: Type): Node()

class MethodDeclaration(val name: String?, val type: Type,val  arguments: List<FormalList>, val variables: List<VarDeclaration>, val statements: List<Statement>, val returnExp: Expression): Node()

class FormalList(val type: Type, val name: String?): Node()

open class Type : Node()
class BooleanType : Type()
class IntType : Type()
class IntArrayType : Type()
class NameIdentifierType(val name: String?) : Type()

open class Statement : Node()
class IfElseStatement(val condition:Expression, val ifStatement:Statement, val elseStatement:Statement) : Statement()
class BlockStatement(val statements: List<Statement>) : Statement()
class PrintStatement(val statement: Expression) : Statement()
class WhileStatement(val condition: Expression, val bodyStatement: Statement) : Statement()
class AssignStatement(val name: String?, val value: Expression) : Statement()
class AssignIndexerStatement(val name: String?, val indexer: Expression, val value: Expression) : Statement()

open class Expression: Node()
class BinaryExpression(val a: Expression, val b: Expression, val tokenType: TokenType) : Expression()
class IndexerExpression(val obj: Expression, val indexer: Expression) : Expression()
class NotExpression(val obj: Expression) : Expression()
class NewExpression(val name: String?) : Expression()
class NewIntArrayExpression(val indexer: Expression) : Expression()
class BracketExpression(val obj: Expression) : Expression()
class LiteralExpression(val value: String?) : Expression()
class NameIdentifierExpression(val name: String?) : Expression()
class ThisExpression : Expression()
class TrueExpression : Expression()
class FalseExpression : Expression()
class LengthExpression(val obj: Expression) : Expression()
class MethodCallExpression(val obj: Expression, val method: String?, val args: List<Expression>) : Expression()