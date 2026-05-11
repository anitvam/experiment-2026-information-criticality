package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.molecules.SimpleMolecule

class WriteMolecule(
    val runningNode: Node<Any?>,
    val moleculeName: String,
    val moleculeValue: Any?
): AbstractAction<Any?>(runningNode) {
    override fun cloneAction(node: Node<Any?>, reaction: Reaction<Any?>): Action<Any?> =
        WriteMolecule(node, moleculeName, moleculeValue)

    override fun execute() =
        runningNode.setConcentration(SimpleMolecule(moleculeName), moleculeValue)

    override fun getContext(): Context = Context.LOCAL
}
