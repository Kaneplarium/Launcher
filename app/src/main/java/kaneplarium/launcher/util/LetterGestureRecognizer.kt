package kaneplarium.launcher.util

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.atan2

object LetterGestureRecognizer {

    fun recognizeLetter(points: List<Offset>): Char? {
        if (points.size < 5) return null

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val width = maxX - minX
        val height = maxY - minY

        if (width < 20f && height < 20f) return null

        val start = points.first()
        val end = points.last()

        val startEndDist = (start - end).getDistance()
        val isClosedLoop = startEndDist < (height * 0.45f) && points.size > 15

        // Check closed loop ('O', 'C', '0')
        if (isClosedLoop) {
            return 'O'
        }

        // Check vertical line ('I', 'L')
        if (height > width * 2.2f) {
            return 'I'
        }

        // Check horizontal line
        if (width > height * 2.2f) {
            return 'H'
        }

        val midPoint = points[points.size / 2]

        // Check V or U shape (Midpoint Y is significantly lower than start & end Y)
        if (midPoint.y > start.y + height * 0.4f && midPoint.y > end.y + height * 0.4f) {
            return 'V'
        }

        // Check A or ^ shape (Midpoint Y is significantly higher than start & end Y)
        if (midPoint.y < start.y - height * 0.4f && midPoint.y < end.y - height * 0.4f) {
            return 'A'
        }

        // Check S shape (multiple X direction changes)
        var xDirectionChanges = 0
        var prevDx = 0f
        for (i in 1 until points.size) {
            val dx = points[i].x - points[i - 1].x
            if (abs(dx) > 2f) {
                if (prevDx != 0f && (dx > 0) != (prevDx > 0)) {
                    xDirectionChanges++
                }
                prevDx = dx
            }
        }

        if (xDirectionChanges >= 2) {
            return 'S'
        }

        // Fallback: Check primary stroke angle
        val angleRad = atan2(end.y - start.y, end.x - start.x)
        val angleDeg = Math.toDegrees(angleRad.toDouble())

        return when {
            angleDeg in -45.0..45.0 -> 'E'
            angleDeg in 45.0..135.0 -> 'M'
            angleDeg in -135.0..-45.0 -> 'T'
            else -> 'K'
        }
    }
}
