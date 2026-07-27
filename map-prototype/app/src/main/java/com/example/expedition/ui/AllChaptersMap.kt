package com.example.expedition.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.hypot
import kotlin.math.sin

private data class MapAnchor(val x: Float, val y: Float)

private data class ChapterMapSpec(
    val assetName: String,
    val anchors: List<MapAnchor>
)

private data class ReflectedMapNode(
    val sourceNode: Any,
    val title: String,
    val position: Int,
    val state: String,
    val pendingClaimIds: List<String>,
    val majorReward: Boolean
)

private fun chapterMapSpec(position: Int): ChapterMapSpec = when (position) {
    1 -> ChapterMapSpec(
        "expedition_chapter_01_northern_gateways.webp",
        listOf(MapAnchor(0.480f, 0.134f), MapAnchor(0.615f, 0.291f), MapAnchor(0.384f, 0.466f), MapAnchor(0.515f, 0.645f), MapAnchor(0.482f, 0.814f))
    )
    2 -> ChapterMapSpec(
        "expedition_chapter_02_amazonian_heart.webp",
        listOf(MapAnchor(0.490f, 0.140f), MapAnchor(0.340f, 0.310f), MapAnchor(0.630f, 0.480f), MapAnchor(0.380f, 0.660f), MapAnchor(0.560f, 0.830f))
    )
    3 -> ChapterMapSpec(
        "expedition_chapter_03_andean_horizons.webp",
        listOf(MapAnchor(0.480f, 0.130f), MapAnchor(0.630f, 0.300f), MapAnchor(0.370f, 0.470f), MapAnchor(0.610f, 0.650f), MapAnchor(0.460f, 0.830f))
    )
    4 -> ChapterMapSpec(
        "expedition_chapter_04_ancient_heights.webp",
        listOf(MapAnchor(0.430f, 0.130f), MapAnchor(0.640f, 0.300f), MapAnchor(0.360f, 0.480f), MapAnchor(0.610f, 0.650f), MapAnchor(0.470f, 0.830f))
    )
    5 -> ChapterMapSpec(
        "expedition_chapter_05_salt_stone_and_stars.webp",
        listOf(MapAnchor(0.500f, 0.130f), MapAnchor(0.340f, 0.300f), MapAnchor(0.640f, 0.480f), MapAnchor(0.380f, 0.660f), MapAnchor(0.560f, 0.830f))
    )
    6 -> ChapterMapSpec(
        "expedition_chapter_06_water_in_motion.webp",
        listOf(MapAnchor(0.450f, 0.130f), MapAnchor(0.640f, 0.300f), MapAnchor(0.370f, 0.480f), MapAnchor(0.610f, 0.650f), MapAnchor(0.470f, 0.830f))
    )
    7 -> ChapterMapSpec(
        "expedition_chapter_07_islands_and_ocean.webp",
        listOf(MapAnchor(0.490f, 0.130f), MapAnchor(0.340f, 0.300f), MapAnchor(0.640f, 0.480f), MapAnchor(0.380f, 0.660f), MapAnchor(0.560f, 0.830f))
    )
    8 -> ChapterMapSpec(
        "expedition_chapter_08_pampas_and_atlantic_shores.webp",
        listOf(MapAnchor(0.440f, 0.130f), MapAnchor(0.630f, 0.300f), MapAnchor(0.360f, 0.480f), MapAnchor(0.610f, 0.650f), MapAnchor(0.460f, 0.830f))
    )
    9 -> ChapterMapSpec(
        "expedition_chapter_09_patagonian_edge.webp",
        listOf(MapAnchor(0.500f, 0.130f), MapAnchor(0.340f, 0.300f), MapAnchor(0.640f, 0.480f), MapAnchor(0.380f, 0.660f), MapAnchor(0.560f, 0.830f))
    )
    else -> ChapterMapSpec(
        "expedition_chapter_10_continental_mosaic.webp",
        listOf(MapAnchor(0.450f, 0.130f), MapAnchor(0.640f, 0.300f), MapAnchor(0.370f, 0.480f), MapAnchor(0.610f, 0.650f), MapAnchor(0.470f, 0.830f))
    )
}

@Composable
fun ExpeditionChapterMap(
    chapter: Any,
    nodes: List<Any>,
    onPrepareClaims: (List<String>) -> Unit,
    onOpenEditorial: (Any) -> Unit
) {
    val chapterPosition = readInt(chapter, "getPosition") ?: 1
    val chapterName = readString(chapter, "getName") ?: "Expedition Chapter"
    val spec = chapterMapSpec(chapterPosition)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column {
            Text(
                text = "CHAPTER $chapterPosition",
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = chapterName,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            AndroidView(
                factory = { context -> ChapterMapView(context) },
                update = { view ->
                    view.updateData(spec, nodes, onPrepareClaims, onOpenEditorial)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
        }
    }
}

private class ChapterMapView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val markerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val markerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var currentAssetName: String? = null
    private var mapBitmap: Bitmap? = null
    private var spec: ChapterMapSpec = chapterMapSpec(1)
    private var reflectedNodes: List<ReflectedMapNode> = emptyList()
    private var prepareClaims: ((List<String>) -> Unit)? = null
    private var openEditorial: ((Any) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true
    }

    fun updateData(
        newSpec: ChapterMapSpec,
        nodes: List<Any>,
        onPrepareClaims: (List<String>) -> Unit,
        onOpenEditorial: (Any) -> Unit
    ) {
        spec = newSpec
        if (currentAssetName != newSpec.assetName) {
            currentAssetName = newSpec.assetName
            mapBitmap?.recycle()
            mapBitmap = runCatching {
                context.assets.open(newSpec.assetName).use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
        reflectedNodes = nodes.take(5).mapIndexedNotNull { index, presentation ->
            val sourceNode = readValue(presentation, "getNode") ?: return@mapIndexedNotNull null
            val stateValue = readValue(presentation, "getState")
            val state = stateValue?.toString()?.uppercase() ?: "LOCKED"
            val pending = (readValue(presentation, "getPendingClaimIds") as? List<*>)
                ?.mapNotNull { it as? String }
                .orEmpty()
            ReflectedMapNode(
                sourceNode = sourceNode,
                title = readString(sourceNode, "getName") ?: "Stage ${index + 1}",
                position = readInt(sourceNode, "getPosition") ?: index + 1,
                state = state,
                pendingClaimIds = pending,
                majorReward = readBoolean(sourceNode, "isMajorReward", "getMajorReward") ?: false
            )
        }
        prepareClaims = onPrepareClaims
        openEditorial = onOpenEditorial
        contentDescription = reflectedNodes.joinToString(", ") { node ->
            "${node.position}. ${node.title}: ${node.state.lowercase()}"
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = mapBitmap
        if (bitmap != null) {
            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(0, 0, width, height),
                bitmapPaint
            )
        } else {
            canvas.drawColor(AndroidColor.rgb(32, 78, 67))
        }

        val now = SystemClock.uptimeMillis()
        reflectedNodes.forEachIndexed { index, node ->
            val anchor = spec.anchors.getOrNull(index) ?: return@forEachIndexed
            drawMarker(canvas, width * anchor.x, height * anchor.y, node, now)
        }
        if (reflectedNodes.any { it.state == "CLAIMABLE" }) {
            postInvalidateDelayed(32L)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        performClick()
        val hitRadius = 36f * density
        reflectedNodes.forEachIndexed { index, node ->
            val anchor = spec.anchors.getOrNull(index) ?: return@forEachIndexed
            val cx = width * anchor.x
            val cy = height * anchor.y
            if (hypot(event.x - cx, event.y - cy) <= hitRadius) {
                when (node.state) {
                    "CLAIMABLE" -> prepareClaims?.invoke(node.pendingClaimIds)
                    "CLAIMED" -> openEditorial?.invoke(node.sourceNode)
                }
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun drawMarker(canvas: Canvas, cx: Float, cy: Float, node: ReflectedMapNode, now: Long) {
        val accent = when (node.state) {
            "CLAIMABLE" -> AndroidColor.rgb(242, 162, 59)
            "CLAIMED" -> AndroidColor.rgb(53, 166, 110)
            "REACHED" -> AndroidColor.rgb(67, 177, 190)
            else -> AndroidColor.rgb(139, 145, 140)
        }
        val baseRadius = (if (node.majorReward) 22f else 20f) * density

        if (node.state == "CLAIMABLE") {
            val pulse = ((sin(now / 260.0) + 1.0) * 0.5).toFloat()
            markerFill.color = withAlpha(accent, (45f + 70f * pulse).toInt())
            canvas.drawCircle(cx, cy, baseRadius + (5f + 6f * pulse) * density, markerFill)
        }

        markerFill.color = AndroidColor.argb(235, 31, 35, 31)
        markerStroke.color = accent
        markerStroke.strokeWidth = (if (node.state == "CLAIMABLE") 3f else 2.2f) * density
        markerFill.setShadowLayer(5f * density, 0f, 2f * density, AndroidColor.argb(150, 0, 0, 0))
        canvas.drawCircle(cx, cy, baseRadius, markerFill)
        markerFill.clearShadowLayer()
        canvas.drawCircle(cx, cy, baseRadius, markerStroke)

        symbolPaint.color = accent
        symbolPaint.strokeWidth = 2.5f * density
        when (node.state) {
            "CLAIMABLE" -> drawReward(canvas, cx, cy, baseRadius)
            "CLAIMED" -> drawCheck(canvas, cx, cy, baseRadius)
            "REACHED" -> drawCompass(canvas, cx, cy, baseRadius)
            else -> drawLock(canvas, cx, cy, baseRadius)
        }

        numberPaint.textSize = 8.5f * density
        numberPaint.color = AndroidColor.argb(220, 255, 255, 255)
        canvas.drawText(node.position.toString(), cx, cy + baseRadius * 0.72f, numberPaint)
    }

    private fun drawCheck(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val path = Path().apply {
            moveTo(cx - radius * 0.42f, cy - radius * 0.02f)
            lineTo(cx - radius * 0.10f, cy + radius * 0.30f)
            lineTo(cx + radius * 0.48f, cy - radius * 0.34f)
        }
        canvas.drawPath(path, symbolPaint)
    }

    private fun drawReward(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val box = RectF(cx - radius * 0.39f, cy - radius * 0.15f, cx + radius * 0.39f, cy + radius * 0.34f)
        canvas.drawRect(box, symbolPaint)
        canvas.drawLine(cx, box.top, cx, box.bottom, symbolPaint)
        canvas.drawLine(box.left, cy, box.right, cy, symbolPaint)
        canvas.drawArc(RectF(cx - radius * 0.37f, cy - radius * 0.43f, cx, cy - radius * 0.02f), 205f, 230f, false, symbolPaint)
        canvas.drawArc(RectF(cx, cy - radius * 0.43f, cx + radius * 0.37f, cy - radius * 0.02f), 105f, 230f, false, symbolPaint)
    }

    private fun drawCompass(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(cx, cy - radius * 0.05f, radius * 0.40f, symbolPaint)
        val path = Path().apply {
            moveTo(cx + radius * 0.10f, cy - radius * 0.29f)
            lineTo(cx - radius * 0.08f, cy + radius * 0.20f)
            lineTo(cx - radius * 0.18f, cy - radius * 0.01f)
            close()
        }
        markerFill.color = symbolPaint.color
        canvas.drawPath(path, markerFill)
    }

    private fun drawLock(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val body = RectF(cx - radius * 0.32f, cy - radius * 0.05f, cx + radius * 0.32f, cy + radius * 0.34f)
        canvas.drawRoundRect(body, radius * 0.08f, radius * 0.08f, symbolPaint)
        canvas.drawArc(RectF(cx - radius * 0.24f, cy - radius * 0.42f, cx + radius * 0.24f, cy + radius * 0.08f), 195f, 150f, false, symbolPaint)
    }
}

private fun readValue(target: Any, vararg methodNames: String): Any? {
    for (name in methodNames) {
        val method = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
            ?: target.javaClass.declaredMethods.firstOrNull { it.name == name && it.parameterCount == 0 }
        if (method != null) {
            val result = runCatching {
                method.isAccessible = true
                method.invoke(target)
            }.getOrNull()
            if (result != null) return result
        }
    }
    return null
}

private fun readString(target: Any, vararg methodNames: String): String? =
    readValue(target, *methodNames) as? String

private fun readInt(target: Any, vararg methodNames: String): Int? =
    (readValue(target, *methodNames) as? Number)?.toInt()

private fun readBoolean(target: Any, vararg methodNames: String): Boolean? =
    readValue(target, *methodNames) as? Boolean

private fun withAlpha(color: Int, alpha: Int): Int =
    AndroidColor.argb(alpha.coerceIn(0, 255), AndroidColor.red(color), AndroidColor.green(color), AndroidColor.blue(color))
