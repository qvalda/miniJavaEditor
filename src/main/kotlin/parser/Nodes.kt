package parser

import tokenizer.TokenType

class NodeLocation(val lineIndex:Int, val startIndex : Int, val endIndex : Int)

open class BaseNode(val location: NodeLocation)

class ProgramNode(val mainClass: MainClassNode, val classes : List<ClassDeclarationNode>) : IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class MainClassNode(val name: String?, val  variables: List<VarDeclarationNode>, val  statements: List<StatementNode>, location: NodeLocation): BaseNode(location),IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class ClassDeclarationNode(val name:String?, val baseName:String?, val variables : List<VarDeclarationNode>, val  methods : List<MethodDeclarationNode>, location: NodeLocation):BaseNode(location), IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class VarDeclarationNode(val name: String?, val type: TypeNodeNode, location: NodeLocation):BaseNode(location), IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class MethodDeclarationNode(val name: String?, val type: TypeNodeNode, val  arguments: List<FormalListNode>, val variables: List<VarDeclarationNode>, val statements: List<StatementNode>, val returnExp: ExpressionNode,
                            location: NodeLocation
):BaseNode(location), IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class FormalListNode(val type: TypeNodeNode, val name: String?): IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

open class TypeNodeNode : IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class BooleanTypeNode : TypeNodeNode()
class IntTypeNode : TypeNodeNode()
class IntArrayTypeNode : TypeNodeNode()
class NameIdentifierTypeNode(val name: String?) : TypeNodeNode()

open class StatementNode : IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class IfElseStatementNode(val condition:ExpressionNode, val ifStatement:StatementNode, val elseStatement:StatementNode) : StatementNode()
class BlockStatementNode(val statements: List<StatementNode>) : StatementNode()
class PrintStatementNode(val statement: ExpressionNode) : StatementNode()
class WhileStatementNode(val condition: ExpressionNode, val bodyStatement: StatementNode) : StatementNode()
class AssignStatementNode(val name: String?, val value: ExpressionNode) : StatementNode()
class AssignIndexerStatementNode(val name: String?, val indexer: ExpressionNode, val value: ExpressionNode) : StatementNode()

open class ExpressionNode: IVisitableNode {
    override fun accept(v: IVisitor) {
        v.visit(this)
    }
}

class BinaryExpressionNode(val a: ExpressionNode, val b: ExpressionNode, val tokenType: TokenType) : ExpressionNode()
class IndexerExpressionNode(val obj: ExpressionNode, val indexer: ExpressionNode) : ExpressionNode()
class NotExpressionNode(val obj: ExpressionNode) : ExpressionNode()
class NewExpressionNode(val name: String?) : ExpressionNode()
class NewIntArrayExpressionNode(val indexer: ExpressionNode) : ExpressionNode()
class BracketExpressionNode(val obj: ExpressionNode) : ExpressionNode()
class LiteralExpressionNode(val value: String?) : ExpressionNode()
class NameIdentifierExpressionNode(val name: String?) : ExpressionNode()
class ThisExpressionNode : ExpressionNode()
class TrueExpressionNode : ExpressionNode()
class FalseExpressionNode : ExpressionNode()
class LengthExpressionNode(val obj: ExpressionNode) : ExpressionNode()
class MethodCallExpressionNode(val obj: ExpressionNode, val method: String?, val args: List<ExpressionNode>) : ExpressionNode()