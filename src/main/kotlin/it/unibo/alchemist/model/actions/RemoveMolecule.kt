package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.molecules.SimpleMolecule

class RemoveMolecule(
    val runningNode: Node<Any?>,
    val moleculeName: String,
): AbstractAction<Any?>(runningNode) {
    val molecule = SimpleMolecule(moleculeName)

    override fun cloneAction(node: Node<Any?>, reaction: Reaction<Any?>): Action<Any?> =
        RemoveMolecule(node, moleculeName)

    override fun execute() =
        when(runningNode.contains(molecule)) {
            true -> runningNode.removeConcentration(SimpleMolecule(moleculeName))
            else -> Unit
        }

    override fun getContext(): Context = Context.LOCAL
}

