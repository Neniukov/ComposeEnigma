package com.neniukov.composeenigma.ui.cosmos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CosmosGeometryTest {
    @Test
    fun `the constellation has distinct unit points and one connected undirected network`() {
        val model = CosmosModel()
        assertEquals(COSMOS_NODE_COUNT, model.nodes.size)
        assertEquals(model.nodes.size, model.nodes.toSet().size)
        model.nodes.forEach { assertEquals(1f, it.distanceTo(CosmosPoint(0f, 0f, 0f)), 0.00001f) }
        assertEquals(model.links.size, model.links.toSet().size)
        assertTrue(
            model.links.all {
                it.from in model.nodes.indices && it.to in model.nodes.indices && it.from < it.to
            },
        )

        val visited = mutableSetOf(0)
        val pending = ArrayDeque<Int>().apply { add(0) }
        while (pending.isNotEmpty()) {
            val node = pending.removeFirst()
            model.links.forEach { link ->
                val next = when (node) {
                    link.from -> link.to
                    link.to -> link.from
                    else -> return@forEach
                }
                if (visited.add(next)) pending.add(next)
            }
        }
        assertEquals(model.nodes.size, visited.size)
    }

    @Test
    fun `rotation preserves every connection length at extreme viewing angles`() {
        val model = CosmosModel()
        for (yaw in listOf(-12f, -1f, 0f, 3f, 18f)) {
            for (pitch in listOf(-1.1f, 0f, 1.1f)) {
                model.links.forEach { link ->
                    val a = model.nodes[link.from]
                    val b = model.nodes[link.to]
                    assertEquals(
                        a.distanceTo(b),
                        cosmosRotate(a, yaw, pitch).distanceTo(cosmosRotate(b, yaw, pitch)),
                        0.00001f,
                    )
                }
            }
        }
    }

    @Test
    fun `perspective magnifies near points and keeps all orbital paths finite`() {
        val viewport = CosmosViewport(1000f, 1000f)
        val near = viewport.project(CosmosPoint(1f, 0f, 1f))
        val far = viewport.project(CosmosPoint(1f, 0f, -1f))
        assertTrue(near.x > far.x)
        assertTrue(near.perspective > far.perspective)
        CosmosModel().orbits.forEach { orbit ->
            assertEquals(orbit.first().x, orbit.last().x, 0.00001f)
            assertEquals(orbit.first().y, orbit.last().y, 0.00001f)
            orbit.forEach { point ->
                val projected = viewport.project(point)
                assertTrue(projected.x.isFinite() && projected.y.isFinite() && projected.perspective > 0f)
            }
        }
    }

    @Test
    fun `the inner filaments stay closed and leave a clear gap to the neural sphere`() {
        val origin = CosmosPoint(0f, 0f, 0f)
        CosmosModel().filaments.forEach { filament ->
            assertEquals(0f, filament.first().distanceTo(filament.last()), 0.00001f)
            filament.forEach { point ->
                assertTrue(point.x.isFinite() && point.y.isFinite() && point.z.isFinite())
                assertTrue(point.distanceTo(origin) < 0.65f)
            }
        }
    }

    @Test
    fun `a signal reaches a neighbour after its source and disappears at completion`() {
        assertTrue(cosmosSignalEnergy(0f, 0.02f) > cosmosSignalEnergy(1f, 0.02f))
        assertTrue(cosmosSignalEnergy(1f, 0.42f) > cosmosSignalEnergy(0f, 0.42f))
        for (step in 0..100) {
            val distance = step / 50f
            assertEquals(0f, cosmosSignalEnergy(distance, 1f))
            assertTrue(cosmosSignalEnergy(distance, 0.4f) in 0f..1f)
        }
        assertEquals(0f, cosmosSignalEnergy(Float.NaN, 0.5f))
        assertEquals(0f, cosmosSignalEnergy(1f, Float.POSITIVE_INFINITY))
    }

    @Test
    fun `tapping a projected front node selects that node after rotation`() {
        val model = CosmosModel()
        val viewport = CosmosViewport(1000f, 900f)
        val yaw = 1.2f
        val pitch = 0.3f
        val index = model.nodes.indices.maxBy { cosmosRotate(model.nodes[it], yaw, pitch).z }
        val point = viewport.project(cosmosRotate(model.nodes[index], yaw, pitch))
        assertEquals(index, cosmosNearestNode(model, viewport, point.x, point.y, yaw, pitch))
    }
}
