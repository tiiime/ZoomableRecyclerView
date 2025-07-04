package io.github.tiiime.manga.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ZoomableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private val matrix = Matrix()
    private val matrixValues = FloatArray(9)

    private var scaleFactor = 1.0f
    private var minScale = 0.95f
    private var maxScale = 3.0f

    private val inverseMatrix = Matrix()
    private val viewRect = FloatArray(2)

    private val scroller = OverScroller(context)
    private val gestureDetector: GestureDetectorCompat
    private val scaleAnimator = ValueAnimator()
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    private var isScaling = false

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            scroller.forceFinished(true)
            scaleAnimator.cancel()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (scaleFactor > 1.0f && !isScaling) {
                matrix.postTranslate(-distanceX, -distanceY)
                adjustBounds()
                invalidate()
                return true
            }
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (scaleFactor > 1.0f && !isScaling) {
                startFling(velocityX.toInt(), velocityY.toInt())
                return true
            }
            return false
        }
    }

    private val scaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            scaleAnimator.cancel()
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val newScale = (scaleFactor * scale).coerceIn(minScale, maxScale)
            val actualScale = newScale / scaleFactor

            if (abs(actualScale - 1f) > 0.001f) {
                scaleFactor = newScale
                matrix.postScale(actualScale, actualScale, detector.focusX, detector.focusY)
                adjustBounds()
                invalidate()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
            if (scaleFactor < 1.0f) {
                startScaleAnimation(detector.focusX, detector.focusY)
            }
        }
    }

    init {
        gestureDetector = GestureDetectorCompat(context, gestureListener)
        scaleAnimator.addUpdateListener { animator ->
            val animatedScale = animator.animatedValue as Float
            matrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            val scaleChange = animatedScale / currentScale
            scaleFactor = animatedScale
            matrix.postScale(scaleChange, scaleChange, width / 2f, height / 2f)
            adjustBounds()
            invalidate()
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            matrix.getValues(matrixValues)
            val currentX = matrixValues[Matrix.MTRANS_X]
            val currentY = matrixValues[Matrix.MTRANS_Y]

            val newX = scroller.currX.toFloat()
            val newY = scroller.currY.toFloat()

            matrix.postTranslate(newX - currentX, newY - currentY)
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        return isScaling || super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val transformedPoint = transformPoint(ev.x, ev.y)
        val fixedY = if (canScrollVertically(1) || canScrollVertically(-1)) {
            transformedPoint[1]
        } else {
            ev.y
        }
        val fixedX = if (canScrollHorizontally(1) || canScrollHorizontally(-1)) {
            transformedPoint[0]
        } else {
            ev.x
        }
        ev.setLocation(fixedX, fixedY)
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    private fun adjustBounds() {
        matrix.getValues(matrixValues)
        var transX = matrixValues[Matrix.MTRANS_X]
        var transY = matrixValues[Matrix.MTRANS_Y]

        val scaledWidth = width * scaleFactor
        val scaledHeight = height * scaleFactor

        transX = if (scaledWidth < width) {
            (width - scaledWidth) / 2
        } else {
            transX.coerceIn(width - scaledWidth, 0f)
        }

        transY = if (scaledHeight < height) {
            (height - scaledHeight) / 2
        } else {
            transY.coerceIn(height - scaledHeight, 0f)
        }

        matrixValues[Matrix.MTRANS_X] = transX
        matrixValues[Matrix.MTRANS_Y] = transY
        matrix.setValues(matrixValues)
    }

    private fun startScaleAnimation(focusX: Float, focusY: Float) {
        scaleAnimator.cancel()
        scaleAnimator.setFloatValues(scaleFactor, 1.0f)
        scaleAnimator.duration = 300
        scaleAnimator.start()
    }

    private fun startFling(velocityX: Int, velocityY: Int) {
        matrix.getValues(matrixValues)
        val currentX = matrixValues[Matrix.MTRANS_X].toInt()
        val currentY = matrixValues[Matrix.MTRANS_Y].toInt()

        val scaledWidth = width * scaleFactor
        val scaledHeight = height * scaleFactor

        val minX = (width - scaledWidth).toInt()
        val maxX = 0
        val minY = (height - scaledHeight).toInt()
        val maxY = 0

        scroller.fling(
            currentX, currentY,
            velocityX, velocityY,
            minX, maxX,
            minY, maxY
        )
        invalidate()
    }

    private fun transformPoint(x: Float, y: Float): FloatArray {
        matrix.invert(inverseMatrix)
        viewRect[0] = x
        viewRect[1] = y
        inverseMatrix.mapPoints(viewRect)
        return viewRect
    }
}