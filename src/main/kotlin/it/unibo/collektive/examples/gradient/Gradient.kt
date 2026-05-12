package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.stdlib.collapse.minBy
import it.unibo.collektive.stdlib.util.Reducer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

inline fun <reified ID : Any, reified Value, reified Distance: Comparable<Distance>> Aggregate<ID>.boundedBellmanFordGradientCast(
    source: Boolean,
    local: Value,
    bottom: Distance,
    top: Distance,
    crossinline shouldPropagate: (Distance, Value) -> Boolean,
    noinline accumulateData: (fromSource: Distance, toNeighbor: Distance, data: Value) -> Value =
        { _, _, data -> data },
    crossinline accumulateDistance: Reducer<Distance>,
    metric: Field<ID, Distance>,
): Pair<Distance, Value> {
    val nullValue = top to local
    return share(nullValue) { distancesAndValues: Field<ID, Pair<Distance, Value>> ->
        val closest: Pair<Distance, Value>? = distancesAndValues
            .alignedMapValues(metric) { (neighborToSource, value: Value), hereToNeighbor ->
                val distance = accumulateDistance(neighborToSource, hereToNeighbor).coerceIn(bottom, top)
                val data = accumulateData(neighborToSource, hereToNeighbor, value)
                distance to data
            }
            .neighbors.values.minBy { it.first }
            ?.takeIf { shouldPropagate(it.first, it.second) }
        when {
            source -> bottom to local
            closest == null -> nullValue
            else -> closest
        }
    }
}

inline fun <reified ID : Any, reified Value> Aggregate<ID>.boundedBellmanFordGradientCast(
    source: Boolean,
    local: Value,
    crossinline shouldPropagate: (Double, Value) -> Boolean,
    metric: Field<ID, Double>,
): Pair<Double, Value> = boundedBellmanFordGradientCast(
    source = source,
    local = local,
    bottom = 0.0,
    top = Double.POSITIVE_INFINITY,
    shouldPropagate = shouldPropagate,
    accumulateDistance = Double::plus,
    metric = metric,
)

fun <P : Position<P>> CollektiveDevice<P>.localPosition() = this.environment.getPosition(this.node)

data class Payload<P: Position<*>>(val position: P, val direction: P, val criticalities: Map<String, Any>)

fun Aggregate<Int>.collektiveProgram(environment: CollektiveDevice<*>): Unit {
    // ==============================================
    // Propagate Incoming Information Criticality Map
    // ==============================================
    val direction = evolve(environment.localPosition()) { previous ->
        environment.localPosition() - previous.coordinates
    }
    boundedBellmanFordGradientCast(
        source = environment["source"] as? Boolean == true,
        local = Payload(
            environment.localPosition(),
            direction,
            mapOf("Criticality" to localId.toDouble()).takeIf { environment["source"] as? Boolean == true }.orEmpty(),
        ),
        shouldPropagate = { _, (sourcePosition, direction) ->
            val (x, y) = (environment.localPosition() - sourcePosition.coordinates).coordinates
            val angle = atan2(y, x)
            val carDirection = atan2(direction.coordinates[1], direction.coordinates[0])
            abs(angle-carDirection) < PI / 4
        },
        metric = with(environment) { distances() },
    ).let { (distance, payload) ->
        when {
            payload.criticalities.isNotEmpty() -> environment["distance"] = distance
            environment.get<Any?>("distance") == Unit -> Unit
            else -> environment.node.removeConcentration(SimpleMolecule("distance"))
        }
    }

    // ==========================================================
    // Propagate Outgoing information when critical event happens
    // ==========================================================
    boundedBellmanFordGradientCast(
        source = environment["event"] as? Double == 1.0,
        local = mapOf("Information" to localId.toDouble()).takeIf { environment["event"] as? Double == 1.0 }.orEmpty(),
        shouldPropagate = { distance, _ -> distance < 100.0 }, // Should not be fixed: the propagation is determined on the Outgoing Information Criticality
        metric = with(environment) { distances() },
    ).let {
        when {
            it.second.isNotEmpty() -> environment["event-distance"] = it.first
            environment.get<Any?>("event-distance") == Unit -> Unit
            else -> environment.node.removeConcentration(SimpleMolecule("event-distance"))
        }
    }

}
