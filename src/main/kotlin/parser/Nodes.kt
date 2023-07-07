package parser

import tokenizer.TokenType

class NodeLocation(val lineIndex:Int, val startIndex : Int, val endIndex : Int)

interface INodeWithLocation {
    val location: NodeLocation
    val name: String
}

class ProgramNode(val mainClass: MainClassNode, val classes : List<ClassDeclarationNode>) : IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class MainClassNode(override val name: String, val  variables: List<VarDeclarationNode>, val  statements: List<StatementNode>, override val location: NodeLocation): INodeWithLocation, IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class ClassDeclarationNode(override val name:String, val baseName:String?, val variables : List<VarDeclarationNode>, val  methods : List<MethodDeclarationNode>,
                           override val location: NodeLocation): INodeWithLocation, IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class VarDeclarationNode(override val name: String, val type: TypeNode,override val location: NodeLocation): INodeWithLocation, IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class MethodDeclarationNode(override val name: String, val type: TypeNode, val  arguments: List<FormalListNode>, val variables: List<VarDeclarationNode>,
                            val statements: List<StatementNode>, val returnExp: ExpressionNode, override val location: NodeLocation): INodeWithLocation, IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class FormalListNode(val type: TypeNode, override val name: String, override val location: NodeLocation): INodeWithLocation, IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

open class TypeNode : IVisitableNode {
    override fun accept(v: IProgramVisitor) {
        v.visit(this)
    }
}

class BooleanTypeNode : TypeNode()
class IntTypeNode : TypeNode()
class IntArrayTypeNode : TypeNode()
class NameIdentifierTypeNode(val name: String?) : TypeNode()

open class StatementNode : IVisitableNode {
    override fun accept(v: IProgramVisitor) {
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
    override fun accept(v: IProgramVisitor) {
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