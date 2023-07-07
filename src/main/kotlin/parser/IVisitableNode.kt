package parser

interface IVisitableNode {
    fun accept(v: IProgramVisitor)
}