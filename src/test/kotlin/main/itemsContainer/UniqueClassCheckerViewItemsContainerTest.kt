package main.itemsContainer

import helpers.Event
import main.model.IParsedModel
import main.model.ITokenizedModel
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import parser.*

class UniqueClassCheckerViewItemsContainerTest {

    @Test
    fun findDuplicates() {
        val program = ProgramNode(
            MainClassNode("mainclass", emptyList(), emptyList(), mock(NodeLocation::class.java)),
            listOf(
                ClassDeclarationNode(
                    name = "class1",
                    baseName = null,
                    variables = listOf(
                        VarDeclarationNode("var1", IntTypeNode(), NodeLocation(1, 0, 0)),
                        VarDeclarationNode("var1", IntTypeNode(), NodeLocation(2, 0, 0)),
                        VarDeclarationNode("var2", IntTypeNode(), NodeLocation(3, 0, 0)),
                    ),
                    methods = emptyList(),
                    location = NodeLocation(1, 10, 20)
                ),
                ClassDeclarationNode(
                    name = "class1",
                    baseName = null,
                    variables = emptyList(),
                    methods = listOf(
                        MethodDeclarationNode(
                            name = "method1",
                            type = IntTypeNode(),
                            arguments = emptyList(),
                            variables = emptyList(),
                            statements = emptyList(),
                            returnExp = ExpressionNode(),
                            location = NodeLocation(3, 0, 0)
                        ),
                        MethodDeclarationNode(
                            name = "method1",
                            type = IntTypeNode(),
                            arguments = emptyList(),
                            variables = emptyList(),
                            statements = emptyList(),
                            returnExp = ExpressionNode(),
                            location = NodeLocation(4, 0, 0)
                        )
                    ),
                    location = NodeLocation(6, 30, 40)
                ),
            )
        )

        val tokenizedModel = mock(ITokenizedModel::class.java)
        val tokenizedModelModified = Event<Unit>()
        `when`(tokenizedModel.modified).thenReturn(tokenizedModelModified)
        val parsedModelResultChanged = Event<Unit>()
        val parsedModel = mock(IParsedModel::class.java)
        `when`(parsedModel.parserResult).thenReturn(ParserResult(program, emptyList()))
        `when`(parsedModel.parserResultChanged).thenReturn(parsedModelResultChanged)

        val model = UniqueClassCheckerViewItemsContainer(tokenizedModel, parsedModel)

        assertEquals(2, model.getItems(1).size)
        assertEquals(1, model.getItems(2).size)
        assertEquals(1, model.getItems(3).size)
        assertEquals(1, model.getItems(4).size)
        assertEquals(0, model.getItems(5).size)
        assertEquals(1, model.getItems(6).size)

        tokenizedModelModified(Unit)

        assertEquals(0, model.getItems(1).size)
        assertEquals(0, model.getItems(2).size)
        assertEquals(0, model.getItems(3).size)
        assertEquals(0, model.getItems(4).size)
        assertEquals(0, model.getItems(5).size)
        assertEquals(0, model.getItems(6).size)
    }
}