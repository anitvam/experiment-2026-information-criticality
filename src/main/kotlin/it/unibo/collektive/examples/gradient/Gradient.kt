package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.stdlib.collapse.min
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gossip
import kotlin.Double.Companion.POSITIVE_INFINITY

fun Aggregate<Int>.gradientEntrypoint(environment: CollektiveDevice<*>): Map<String, Double>  {
    val criticalities = mapOf("Criticality" to 100.0)
    with(environment) {
        val leaderID = 200 //TODO(Take this from alchemist property)
        val maxDistanceReachedByCriticality = 150

        val srd = share(criticalities) { field ->
            val distance =  distanceTo(localId == leaderID, distances())
            environment["distance"] = distance

            when (distance < maxDistanceReachedByCriticality) {
                true -> criticalities
                else -> emptyMap()
            }
        }
        environment["shared"] = srd
        environment["shared-contains-value"] = srd.isNotEmpty()

        return srd
    }
}
