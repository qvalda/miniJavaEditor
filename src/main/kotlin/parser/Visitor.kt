package parser

interface IVisitableNode {
    fun accept(v: IVisitor)
}

interface IVisitor {
    fun visit(node: ProgramNode) {}
    fun visit(node: MainClassNode) {}
    fun visit(node: ClassDeclarationNode) {}
    fun visit(node: StatementNode) {}
    fun visit(node: VarDeclarationNode) {}
    fun visit(node: MethodDeclarationNode) {}
    fun visit(node: FormalListNode) {}
    fun visit(node: TypeNodeNode) {}
    fun visit(node: ExpressionNode) {}
}