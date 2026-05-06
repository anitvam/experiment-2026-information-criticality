package it.unibo.alchemist.model.actions

import com.google.common.collect.ImmutableList
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.routes.PolygonalChain
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CircularMovement<T, P: Position2D<P>>(
    environment: Environment<T, P>,
    node: Node<T>,
    reaction: Reaction<T>,
    val radius: Double,
    val center: List<Double>,
    val speed: Double,
): AbstractConfigurableMoveNode<T, P>(
    environment,
    node,
    { p1, p2 -> PolygonalChain(ImmutableList.of(p1, p2)) },
    CircularTargetSelectionStrategy(
        environment,
        node,
        radius,
        environment.makePosition(center.first(), center[1]),
        speed
    ),
    ConstantSpeed(reaction, speed)
) {
    override fun interpolatePositions(current: P, target: P, maxWalk: Double): P {
        val vector = target - current.coordinates
        if (current.distanceTo(target) < maxWalk) return target
        val angle = atan2(vector.y, vector.x)
        return environment.makePosition(
            maxWalk * cos(angle),
            maxWalk * sin(angle)
        )
    }

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): Action<T> = CircularMovement(
        environment,
        node,
        reaction,
        radius,
        center,
        speed,
    )

}

class CircularTargetSelectionStrategy<T, P : Position2D<P>>(
    val environment: Environment<T, P>,
    val node: Node<T>,
    val radius: Double,
    val center: P,
    val speed: Double,
): TargetSelectionStrategy<T, P> {
    override fun getTarget(): P = positionInCircumference(radius, center)

    private fun positionInCircumference(radius: Double, center: P): P {
        val current = environment.getPosition(node)
        val angle = atan2(current.y - center.y, current.x - center.x)
        val newAngle = angle + Math.toRadians(speed)
        val newX = center.x + radius * cos(newAngle)
        val newY = center.y + radius * sin(newAngle)
        return environment.makePosition(newX, newY)
    }
}

