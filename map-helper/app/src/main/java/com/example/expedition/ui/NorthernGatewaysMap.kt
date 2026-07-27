@file:JvmName("NorthernGatewaysMapKt")

package com.example.expedition.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.lang.reflect.Method
import kotlin.math.hypot
import kotlin.math.sin

private const val MAP_ASSET_NAME = "expedition_chapter_01_northern_gateways.webp"

private data class MapAnchor(val x: Float, val y: Float)

private val mapAnchors = listOf(
    MapAnchor(0.480f, 0.134f),
    MapAnchor(0.615f, 0.291f),
    MapAnchor(0.384f, 0.466f),
    MapAnchor(0.515f, 0.645f),
    MapAnchor(0.482f, 0.814f)
)

private data class ReflectedMapNode(
    val node: Any,
    val title: String,
    val position: Int,
    val state: String,
    val pendingClaimIds: List<String>,
    val majorReward: Boolean
)

@Composable
fun NorthernGatewaysChapterMap(
    chapter: Any,
    nodes: List<Any>,
    onPrepareClaims: (List<String>) -> Unit,
    onOpenEditorial: (Any) -> Unit
) {
    val chapterPosition = readInt(chapter, "getPosition") ?: 1
    val chapterName = readString(chapter, "getName") ?: "Northern Gateways"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "CHAPTER $chapterPosition",
                    color = ComposeColor(0xFF0B7C74),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = chapterName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Follow the route and tap the highlighted expedition points.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AndroidView(
                factory = { context -> NorthernGatewaysMapView(context) },
                update = { view ->
                    view.updateData(nodes, onPrepareClaims, onOpenEditorial)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
        }
    }
}

private class NorthernGatewaysMapView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = false
        isDither = false
    }
    private val markerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val markerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 10f * density
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val mapBitmap: Bitmap? = runCatching {
        context.assets.open(MAP_ASSET_NAME).use(BitmapFactory::decodeStream)
    }.getOrNull()

    private var reflectedNodes: List<ReflectedMapNode> = emptyList()
    private var prepareClaims: (List<String>) -> Unit = {}
    private var openEditorial: (Any) -> Unit = {}

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true
        contentDescription = "Northern Gateways expedition map"
    }

    fun updateData(
        nodes: List<Any>,
        onPrepareClaims: (List<String>) -> Unit,
        onOpenEditorial: (Any) -> Unit
    ) {
        reflectedNodes = nodes.take(mapAnchors.size).mapIndexedNotNull { index, presentation ->
            val node = readValue(presentation, "getNode") ?: return@mapIndexedNotNull null
            val stateObject = readValue(presentation, "getState")
            val stateName = (stateObject as? Enum<*>)?.name
                ?: stateObject?.toString()?.substringAfterLast('.')?.uppercase()
                ?: "LOCKED"
            @Suppress("UNCHECKED_CAST")
            val claims = (readValue(presentation, "getPendingClaimIds") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
            ReflectedMapNode(
                node = node,
                title = readString(node, "getName") ?: "Stage ${index + 1}",
                position = readInt(node, "getPosition") ?: index + 1,
                state = stateName,
                pendingClaimIds = claims,
                majorReward = readBoolean(node, "isMajorReward", "getMajorReward") ?: false
            )
        }
        prepareClaims = onPrepareClaims
        openEditorial = onOpenEditorial
        contentDescription = reflectedNodes.joinToString(", ") { "${it.position}. ${it.title}: ${it.state.lowercase()}" }
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
            canvas.drawColor(Color.rgb(19, 70, 65))
        }

        val now = SystemClock.uptimeMillis()
        reflectedNodes.forEachIndexed { index, node ->
            val anchor = mapAnchors.getOrNull(index) ?: return@forEachIndexed
            val cx = width * anchor.x
            val cy = height * anchor.y
            drawMarker(canvas, cx, cy, node, now)
        }

        if (reflectedNodes.any { it.state == "CLAIMABLE" }) {
            postInvalidateDelayed(32L)
        }
    }

    private fun drawMarker(canvas: Canvas, cx: Float, cy: Float, node: ReflectedMapNode, now: Long) {
        val baseRadius = (if (node.majorReward) 22f else 20f) * density
        val accent = when (node.state) {
            "CLAIMED" -> Color.rgb(53, 166, 110)
            "CLAIMABLE" -> Color.rgb(242, 162, 59)
            "REACHED" -> Color.rgb(67, 177, 190)
            else -> Color.rgb(139, 145, 140)
        }
        if (node.state == "CLAIMABLE") {
            val pulse = ((sin(now / 260.0) + 1.0) * 0.5).toFloat()
            markerFill.color = withAlpha(accent, (45 + pulse * 70).toInt())
            canvas.drawCircle(cx, cy, baseRadius + (5f + pulse * 6f) * density, markerFill)
        }

        markerFill.color = Color.argb(235, 31, 35, 31)
        markerStroke.color = accent
        markerStroke.strokeWidth = if (node.state == "CLAIMABLE") 3f * density else 2.2f * density
        markerFill.setShadowLayer(5f * density, 0f, 2f * density, Color.argb(150, 0, 0, 0))
        canvas.drawCircle(cx, cy, baseRadius, markerFill)
        markerFill.clearShadowLayer()
        canvas.drawCircle(cx, cy, baseRadius, markerStroke)

        symbolPaint.color = accent
        symbolPaint.strokeWidth = 2.5f * density
        when (node.state) {
            "CLAIMED" -> drawCheck(canvas, cx, cy, baseRadius)
            "CLAIMABLE" -> drawReward(canvas, cx, cy, baseRadius)
            "REACHED" -> drawCompass(canvas, cx, cy, baseRadius)
            else -> drawLock(canvas, cx, cy, baseRadius)
        }

        numberPaint.textSize = 8.5f * density
        numberPaint.color = Color.argb(220, 255, 255, 255)
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
        val box = RectF(
            cx - radius * 0.39f,
            cy - radius * 0.15f,
            cx + radius * 0.39f,
            cy + radius * 0.34f
        )
        canvas.drawRect(box, symbolPaint)
        canvas.drawLine(cx, box.top, cx, box.bottom, symbolPaint)
        canvas.drawLine(box.left, cy, box.right, cy, symbolPaint)
        canvas.drawArc(
            RectF(cx - radius * 0.37f, cy - radius * 0.43f, cx, cy - radius * 0.02f),
            205f,
            230f,
            false,
            symbolPaint
        )
        canvas.drawArc(
            RectF(cx, cy - radius * 0.43f, cx + radius * 0.37f, cy - radius * 0.02f),
            105f,
            230f,
            false,
            symbolPaint
        )
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
        val body = RectF(
            cx - radius * 0.32f,
            cy - radius * 0.05f,
            cx + radius * 0.32f,
            cy + radius * 0.34f
        )
        canvas.drawRoundRect(body, radius * 0.08f, radius * 0.08f, symbolPaint)
        canvas.drawArc(
            RectF(cx - radius * 0.24f, cy - radius * 0.42f, cx + radius * 0.24f, cy + radius * 0.08f),
            195f,
            150f,
            false,
            symbolPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val selected = reflectedNodes.mapIndexedNotNull { index, node ->
            val anchor = mapAnchors.getOrNull(index) ?: return@mapIndexedNotNull null
            val distance = hypot(event.x - width * anchor.x, event.y - height * anchor.y)
            Triple(index, node, distance)
        }.minByOrNull { it.third } ?: return true

        if (selected.third > 42f * density) return true
        performClick()
        when (selected.second.state) {
            "CLAIMABLE" -> if (selected.second.pendingClaimIds.isNotEmpty()) {
                prepareClaims(selected.second.pendingClaimIds)
            }
            "CLAIMED" -> openEditorial(selected.second.node)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

private fun readValue(target: Any, vararg methodNames: String): Any? {
    for (name in methodNames) {
        val method: Method = target.javaClass.methods.firstOrNull {
            it.name == name && it.parameterTypes.isEmpty()
        } ?: continue
        val value = runCatching { method.invoke(target) }.getOrNull()
        if (value != null) return value
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
    Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
