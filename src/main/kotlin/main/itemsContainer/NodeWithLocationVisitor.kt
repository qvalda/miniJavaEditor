package main.itemsContainer

import parser.*

open class NodeWithLocationVisitor: IProgramVisitor {
    override fun visit(node: ProgramNode) {
        node.mainClass.accept(this)
        node.classes.forEach { it.accept(this) }
    }
    override fun visit(node: MainClassNode) {
        node.variables.forEach { it.accept(this) }
        node.statements.forEach { it.accept(this) }
    }
    override fun visit(node: ClassDeclarationNode) {
        node.variables.forEach { it.accept(this) }
        node.methods.forEach { it.accept(this) }
    }
    override fun visit(node: MethodDeclarationNode) {
        node.arguments.forEach { it.accept(this) }
        node.variables.forEach { it.accept(this) }
        node.statements.forEach { it.accept(this) }
    }
}