package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.stdlib.collapse.minBy
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.util.Reducer

inline fun <reified ID : Any, reified Value, reified Distance: Comparable<Distance>> Aggregate<ID>.boundedBellmanFordGradientCast(
    source: Boolean,
    local: Value,
    bottom: Distance,
    top: Distance,
    distanceBound: Distance,
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
            ?.takeIf { it.first <= distanceBound }
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
    distanceBound: Double,
    metric: Field<ID, Double>,
): Pair<Double, Value> = boundedBellmanFordGradientCast(
    source = source,
    local = local,
    bottom = 0.0,
    top = Double.POSITIVE_INFINITY,
    distanceBound = distanceBound,
    accumulateDistance = Double::plus,
    metric = metric,
)

fun Aggregate<Int>.gradientEntrypoint(environment: CollektiveDevice<*>): Map<String, Double> = boundedBellmanFordGradientCast(
    source = localId == 200,
    local = mapOf("Criticality" to localId.toDouble()).takeIf { localId == 200 }.orEmpty(),
    distanceBound = 150.0,
    metric = with(environment) { distances() },
).let {
    when {
        it.second.isNotEmpty() -> environment["distance"] = it.first
        environment.get<Any?>("distance") == Unit -> Unit
        else -> environment.node.removeConcentration(SimpleMolecule("distance"))
    }
//    environment["shared-contains-value"] = it.second.isNotEmpty()
    it.second
}
//fun Aggregate<Int>.gradientEntrypoint(environment: CollektiveDevice<*>): Map<String, Double>  {
//    val criticalities = mapOf("Criticality" to 100.0)
//    with(environment) {
//        val leaderID = 200 //TODO(Take this from alchemist property)
//        val maxDistanceReachedByCriticality = 150
//
//        val srd = share(criticalities) { _ ->
//            val distance =  distanceTo(localId == leaderID, distances())
//            environment["distance"] = distance
//
//            when (distance < maxDistanceReachedByCriticality) {
//                true -> criticalities
//                else -> emptyMap()
//            }
//        }
//        environment["shared"] = srd
//        environment["shared-contains-value"] = srd.isNotEmpty()
//
//        return srd
//    }
//}
