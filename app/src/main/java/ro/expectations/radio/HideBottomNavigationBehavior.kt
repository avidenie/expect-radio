package ro.expectations.radio

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HideBottomNavigationBehavior<V : View>(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<V>(context, attrs) {

    private var height = 0

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        val paramsCompat = child.layoutParams as ViewGroup.MarginLayoutParams
        height = child.measuredHeight + paramsCompat.topMargin + paramsCompat.bottomMargin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return if (dependency is FrameLayout && dependency.id == R.id.bottomSheet) {
            true
        } else super.layoutDependsOn(parent, child, dependency)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return if (dependency is FrameLayout && dependency.id == R.id.bottomSheet) {
            val bottomSheetBehaviour = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior
            val translationY = Math.min(height, (dependency.height - dependency.top - bottomSheetBehaviour.peekHeight) / 4)
            child.translationY = translationY.toFloat()
            true
        } else super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: V, dependency: View) {
        super.onDependentViewRemoved(parent, child, dependency)
        child.translationY = 0.0f
    }
}