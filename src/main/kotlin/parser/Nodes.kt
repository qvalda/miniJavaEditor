package parser

import tokenizer.TokenType

open class Node(){}

class Program(val mainClass: MainClass, val classes : Array<ClassDeclaration>) : Node() {}

class MainClass(val name: String?,val  variables: Array<VarDeclaration>,val  statements: Array<Statement>): Node(){}

class ClassDeclaration(val name:String?,val variables : Array<VarDeclaration>,val  methods : Array<MethodDeclaration>): Node(){}

open class Type(): Node(){}
class BooleanType() : Type(){}
class IntType() : Type(){}
class IntArrayType() : Type(){}
class NameIdentifierType(val name: String?) : Type(){}

class VarDeclaration(val name: String?, val type: Type): Node() {}

class MethodDeclaration(val name: String?, val type: Type,val  arguments: Array<FormalList>, val variables: Array<VarDeclaration>, val statements: Array<Statement>, val returnExp: Expression): Node(){}

open class Statement(): Node(){}
class IfElseStatement(val condition:Expression, val ifStatement:Statement, val elseStatement:Statement) : Statement(){}
class BlockStatement(val statements: Array<Statement>) : Statement(){}
class WhileStatement(val condition: Expression, val bodyStatement: Statement) : Statement(){}
class AssignStatement(val name: String?, val value: Expression) : Statement(){}
class AssignIndexerStatement(val name: String?, val indexer: Expression, val value: Expression) : Statement(){}

open class Expression: Node(){}
class BinaryExpression(val a: Expression, val b: Expression, val tokenType: TokenType) : Expression() {}
class IndexerExpression(val obj: Expression, val indexer: Expression) : Expression() {}
class NotExpression(val obj: Expression) : Expression() {}
class NewExpression(val name: String?) : Expression() {}
class NewIntArrayExpression(val indexer: Expression) : Expression() {}
class BracketExpression(val obj: Expression) : Expression() {}
class LiteralExpression(val value: String?) : Expression() {}
class NameIdentifierExpression(val name: String?) : Expression() {}
class ThisExpression() : Expression() {}
class TrueExpression() : Expression() {}
class FalseExpression() : Expression() {}
class MethodCallExpression(val obj: Expression, val method: String?, val args: Array<Expression>) : Expression() {}

class FormalList(val type: Type, val name: String?): Node() {}