package parser

interface IProgramVisitor {
    fun visit(node: ProgramNode) {}
    fun visit(node: MainClassNode) {}
    fun visit(node: ClassDeclarationNode) {}
    fun visit(node: StatementNode) {}
    fun visit(node: VarDeclarationNode) {}
    fun visit(node: MethodDeclarationNode) {}
    fun visit(node: FormalListNode) {}
    fun visit(node: TypeNode) {}
    fun visit(node: ExpressionNode) {}
}