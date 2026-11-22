package com.neniukov.composeenigma.ui.paper

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.neniukov.composeenigma.ui.theme.PaperColors as C

/** Static paper polygons in model units; transforms and projection live separately. */
internal class PaperModel {
    val book = buildList {
        for (leaf in PaperLeaf.entries) {
            val left = if (leaf == PaperLeaf.Left) -2.12f else 0f
            val right = if (leaf == PaperLeaf.Left) 0f else 2.12f
            addAll(
                box(
                    x1 = left,
                    x2 = right,
                    y1 = -0.16f,
                    y2 = -0.105f,
                    z1 = -1.50f,
                    z2 = 1.50f,
                    top = C.Cover,
                    edge = C.CoverEdge,
                    leaf = leaf,
                ),
            )
            addAll(
                box(
                    x1 = left + if (leaf == PaperLeaf.Left) 0.05f else 0f,
                    x2 = right - if (leaf == PaperLeaf.Right) 0.05f else 0f,
                    y1 = -0.10f,
                    y2 = 0f,
                    z1 = -1.44f,
                    z2 = 1.44f,
                    top = if (leaf == PaperLeaf.Left) C.PageLight else C.Page,
                    edge = C.PageEdge,
                    leaf = leaf,
                ),
            )
        }
    }

    val prints = buildList {
        add(
            floor(
                listOf(
                    -1.88f to 0.46f,
                    -1.55f to 0.19f,
                    -1.02f to 0.25f,
                    -0.72f to 0.52f,
                    -0.36f to 0.70f,
                    -0.52f to 1.13f,
                    -1.16f to 1.28f,
                    -1.77f to 1.04f,
                ),
                C.Water,
            ),
        )
        add(
            floor(
                listOf(
                    -1.78f to 0.54f,
                    -1.46f to 0.35f,
                    -1.08f to 0.38f,
                    -0.80f to 0.62f,
                    -0.49f to 0.77f,
                    -0.64f to 0.94f,
                    -1.11f to 0.83f,
                    -1.53f to 0.88f,
                ),
                C.WaterLight,
            ),
        )
        add(
            floor(
                listOf(
                    0.15f to -0.50f,
                    1.75f to -0.64f,
                    1.96f to 0.62f,
                    1.50f to 1.18f,
                    0.25f to 0.95f,
                ),
                C.Grass,
                PaperLeaf.Right,
            ),
        )
        add(
            floor(
                listOf(
                    0.68f to 0.46f,
                    0.83f to 0.42f,
                    1.08f to 0.75f,
                    0.75f to 1.20f,
                    0.68f to 1.44f,
                    0.45f to 1.44f,
                    0.51f to 1.10f,
                    0.85f to 0.73f,
                ),
                C.PageLight,
                PaperLeaf.Right,
            ),
        )
    }

    val scenery = buildList {
        // The sun is a paper disc, lifted on a narrow tab behind the peaks.
        add(cutout(circle(0f, 1.20f, 0.34f), C.Sun, -1.05f, -1.05f, start = 0.08f, end = 0.90f))
        add(
            cutout(
                listOf(-0.035f to 0f, 0.035f to 0f, 0.035f to 1.2f, -0.035f to 1.2f),
                C.Gold,
                -1.05f,
                -1.055f,
                start = 0.08f,
                end = 0.90f,
                shadow = false,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.93f to 0f,
                    -0.74f to 0.60f,
                    -0.25f to 1.54f,
                    0.13f to 0.94f,
                    0.43f to 1.26f,
                    0.91f to 0.30f,
                    0.94f to 0f,
                ),
                C.Mountain,
                -1.05f,
                -0.72f,
            ),
        )
        add(
            cutout(
                listOf(-0.25f to 1.54f, 0.13f to 0.94f, -0.13f to 0.58f, -0.40f to 0.78f),
                C.MountainShade,
                -1.05f,
                -0.709f,
                shadow = false,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.46f to 1.14f,
                    -0.25f to 1.54f,
                    -0.02f to 1.18f,
                    -0.18f to 1.24f,
                    -0.30f to 1.10f,
                ),
                C.Snow,
                -1.05f,
                -0.698f,
                shadow = false,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.84f to 0f,
                    -0.59f to 0.55f,
                    -0.10f to 1.75f,
                    0.38f to 0.88f,
                    0.57f to 1.14f,
                    0.94f to 0.18f,
                    0.94f to 0f,
                ),
                C.Mountain,
                1.02f,
                -0.84f,
            ),
        )
        add(
            cutout(
                listOf(-0.10f to 1.75f, 0.38f to 0.88f, 0.10f to 0.43f, -0.31f to 0.84f),
                C.MountainShade,
                1.02f,
                -0.827f,
                shadow = false,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.33f to 1.22f,
                    -0.10f to 1.75f,
                    0.16f to 1.27f,
                    -0.01f to 1.33f,
                    -0.13f to 1.16f,
                ),
                C.Snow,
                1.02f,
                -0.814f,
                shadow = false,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.96f to 0f,
                    -0.90f to 0.23f,
                    -0.52f to 0.47f,
                    -0.12f to 0.26f,
                    0.28f to 0.48f,
                    0.89f to 0.18f,
                    0.93f to 0f,
                ),
                C.Hill,
                -1.02f,
                -0.34f,
                start = 0.10f,
            ),
        )
        add(
            cutout(
                listOf(
                    -0.90f to 0f,
                    -0.65f to 0.30f,
                    -0.20f to 0.47f,
                    0.25f to 0.23f,
                    0.63f to 0.39f,
                    0.91f to 0f,
                ),
                C.Pine,
                1.03f,
                -0.37f,
                start = 0.10f,
            ),
        )
        tree(-1.77f, 0.17f, 0.85f, C.Forest)
        tree(-0.48f, 0.11f, 0.66f, C.Pine)
        tree(-1.47f, 1.05f, 0.42f, C.Pine)
        tree(1.72f, 0.59f, 1.04f, C.Forest)
        tree(0.23f, 0.36f, 0.54f, C.Pine)
        tree(1.43f, -0.05f, 0.70f, C.Pine)
        addAll(house())
        // A tiny jetty and its individual planks sit at the edge of the lake.
        repeat(5) { i ->
            val x = -1.22f + i * 0.075f
            add(floor(listOf(x to 0.73f, x + 0.063f to 0.73f, x + 0.063f to 1.12f, x to 1.12f), C.Trunk))
        }
    }

    val coverFoil = buildList {
        add(
            PaperFace(
                listOf(
                    PaperPoint(0.17f, -0.164f, -1.30f),
                    PaperPoint(1.92f, -0.164f, -1.30f),
                    PaperPoint(1.92f, -0.164f, 1.30f),
                    PaperPoint(0.17f, -0.164f, 1.30f),
                ),
                C.Gold,
                PaperLeaf.Right,
            ),
        )
        add(
            PaperFace(
                listOf(
                    PaperPoint(0.48f, -0.166f, 0.47f),
                    PaperPoint(1.0f, -0.166f, -0.44f),
                    PaperPoint(1.62f, -0.166f, 0.47f),
                ),
                C.Gold,
                PaperLeaf.Right,
            ),
        )
        add(
            PaperFace(
                listOf(
                    PaperPoint(0.91f, -0.166f, 0.47f),
                    PaperPoint(1.38f, -0.166f, -0.13f),
                    PaperPoint(1.75f, -0.166f, 0.47f),
                ),
                C.Gold,
                PaperLeaf.Right,
            ),
        )
        add(
            PaperFace(
                circle(1.44f, -0.61f, 0.11f).map { (x, z) -> PaperPoint(x, -0.167f, z) },
                C.Gold,
                PaperLeaf.Right,
                outline = false,
            ),
        )
    }

    private fun MutableList<PaperFace>.tree(x: Float, z: Float, height: Float, color: Color) {
        val w = height * 0.30f
        val direction = if (z + height > 1.4f) -1f else 1f
        add(
            cutout(
                listOf(-0.028f to 0f, 0.028f to 0f, 0.028f to height * 0.58f, -0.028f to height * 0.58f),
                C.Trunk,
                x,
                z,
                start = 0.14f,
                direction = direction,
            ),
        )
        val silhouette = listOf(
            -w to height * 0.20f, -w * 0.58f to height * 0.47f,
            -w * 0.85f to height * 0.47f, -w * 0.35f to height * 0.73f,
            -w * 0.55f to height * 0.73f, 0f to height,
            w * 0.55f to height * 0.73f, w * 0.35f to height * 0.73f,
            w * 0.85f to height * 0.47f, w * 0.58f to height * 0.47f, w to height * 0.20f,
        )
        add(cutout(silhouette, color, x, z + 0.008f, start = 0.14f, direction = direction))
        add(
            cutout(
                listOf(
                    0f to height * 0.20f,
                    0f to height,
                    w * 0.55f to height * 0.73f,
                    w * 0.35f to height * 0.73f,
                    w * 0.85f to height * 0.47f,
                    w * 0.58f to height * 0.47f,
                    w to height * 0.20f,
                ),
                C.Forest,
                x,
                z + 0.016f,
                start = 0.14f,
                shadow = false,
                direction = direction,
            ),
        )
    }

    private fun house(): List<PaperFace> = buildList {
        val halfWidth = PaperHouseDimensions.WALL_HALF_WIDTH
        val wallHeight = PaperHouseDimensions.WALL_HEIGHT
        fun panel(
            points: List<PaperPoint>,
            color: Color,
            fold: PaperFold,
            anchor: PaperPoint,
            direction: Float = 1f,
            shadow: Boolean = false,
            outline: Boolean = true,
            decorationOf: PaperFace? = null,
        ) = PaperFace(
            points = points,
            color = color,
            leaf = PaperLeaf.Right,
            fold = fold,
            anchor = anchor,
            start = 0.18f,
            end = 0.96f,
            castsShadow = shadow,
            outline = outline,
            foldDirection = direction,
            decorationOf = decorationOf,
        )
        val front = PaperPoint(0.89f, 0f, 0.65f)
        val gable = listOf(
            PaperPoint(-halfWidth, 0f, 0f),
            PaperPoint(halfWidth, 0f, 0f),
            PaperPoint(halfWidth, wallHeight, 0f),
            PaperPoint(0f, 0.76f, 0f),
            PaperPoint(-halfWidth, wallHeight, 0f),
        )
        val facade = panel(gable, C.Clay, PaperFold.Facade, front, shadow = true)
        add(facade)
        val wall =
            listOf(
                PaperPoint(0f, 0f, 0f),
                PaperPoint(0.53f, 0f, 0f),
                PaperPoint(0.53f, wallHeight, 0f),
                PaperPoint(0f, wallHeight, 0f),
            )
        val leftWall = panel(wall, C.PageLight, PaperFold.SideWall, front, direction = -1f)
        add(leftWall)
        add(panel(wall, C.Clay, PaperFold.SideWall, front))
        val span = PaperHouseDimensions.roofSpan
        val roof =
            listOf(
                PaperPoint(0f, 0f, 0f),
                PaperPoint(span, 0f, 0f),
                PaperPoint(span, 0.60f, 0f),
                PaperPoint(0f, 0.60f, 0f),
            )
        add(panel(roof, C.Roof, PaperFold.Roof, front, direction = -1f))
        add(panel(roof, C.Clay, PaperFold.Roof, front))
        add(
            panel(
                listOf(
                    PaperPoint(-0.075f, 0f, 0.008f),
                    PaperPoint(0.075f, 0f, 0.008f),
                    PaperPoint(0.075f, 0.30f, 0.008f),
                    PaperPoint(-0.075f, 0.30f, 0.008f),
                ),
                C.CoverEdge,
                PaperFold.Facade,
                front,
                outline = false,
                decorationOf = facade,
            ),
        )
        for (x in listOf(-0.22f, 0.17f)) {
            add(
                panel(
                    listOf(
                        PaperPoint(x, 0.29f, 0.012f),
                        PaperPoint(x + 0.10f, 0.29f, 0.012f),
                        PaperPoint(x + 0.10f, 0.41f, 0.012f),
                        PaperPoint(x, 0.41f, 0.012f),
                    ),
                    C.Window,
                    PaperFold.Facade,
                    front,
                    outline = false,
                    decorationOf = facade,
                ),
            )
        }
        add(
            panel(
                circle(0f, 0.58f, 0.055f).map { (x, y) -> PaperPoint(x, y, 0.015f) },
                C.Window,
                PaperFold.Facade,
                front,
                outline = false,
                decorationOf = facade,
            ),
        )
        add(
            panel(
                listOf(
                    PaperPoint(0.17f, 0.25f, 0.008f),
                    PaperPoint(0.35f, 0.25f, 0.008f),
                    PaperPoint(0.35f, 0.40f, 0.008f),
                    PaperPoint(0.17f, 0.40f, 0.008f),
                ),
                C.Window,
                PaperFold.SideWall,
                front,
                direction = -1f,
                outline = false,
                decorationOf = leftWall,
            ),
        )
    }

    private fun cutout(
        points: List<Pair<Float, Float>>,
        color: Color,
        x: Float,
        z: Float,
        start: Float = 0.05f,
        end: Float = 0.94f,
        shadow: Boolean = true,
        direction: Float = 1f,
    ) = PaperFace(
        points = points.map { (u, v) -> PaperPoint(u, v, 0f) },
        color = color,
        leaf = if (x < 0f) PaperLeaf.Left else PaperLeaf.Right,
        fold = PaperFold.Cutout,
        anchor = PaperPoint(x, 0f, z),
        start = start,
        end = end,
        castsShadow = shadow,
        outline = shadow,
        foldDirection = direction,
    )

    private fun floor(points: List<Pair<Float, Float>>, color: Color, leaf: PaperLeaf = PaperLeaf.Left) = PaperFace(
        points.map { (x, z) ->
            PaperPoint(x, 0.012f, z)
        },
        color,
        leaf,
        outline = false,
    )

    private fun box(
        x1: Float,
        x2: Float,
        y1: Float,
        y2: Float,
        z1: Float,
        z2: Float,
        top: Color,
        edge: Color,
        leaf: PaperLeaf,
    ): List<PaperFace> = listOf(
        PaperFace(
            listOf(PaperPoint(x1, y2, z1), PaperPoint(x2, y2, z1), PaperPoint(x2, y2, z2), PaperPoint(x1, y2, z2)),
            top,
            leaf,
        ),
        PaperFace(
            listOf(PaperPoint(x1, y1, z2), PaperPoint(x2, y1, z2), PaperPoint(x2, y2, z2), PaperPoint(x1, y2, z2)),
            edge,
            leaf,
        ),
        PaperFace(
            listOf(PaperPoint(x1, y1, z1), PaperPoint(x1, y1, z2), PaperPoint(x1, y2, z2), PaperPoint(x1, y2, z1)),
            edge,
            leaf,
        ),
        PaperFace(
            listOf(PaperPoint(x2, y1, z2), PaperPoint(x2, y1, z1), PaperPoint(x2, y2, z1), PaperPoint(x2, y2, z2)),
            edge,
            leaf,
        ),
        PaperFace(
            listOf(PaperPoint(x1, y1, z1), PaperPoint(x2, y1, z1), PaperPoint(x2, y1, z2), PaperPoint(x1, y1, z2)),
            top,
            leaf,
        ),
    )

    private fun circle(x: Float, y: Float, radius: Float) = List(40) { i ->
        val angle = i * 2f * PI.toFloat() / 40f
        (x + cos(angle) * radius) to (y + sin(angle) * radius)
    }
}
