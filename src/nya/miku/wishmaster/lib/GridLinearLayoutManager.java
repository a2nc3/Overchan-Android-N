package nya.miku.wishmaster.lib;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class GridLinearLayoutManager {

    private LinearLayout managedLinearLayout;

    public GridLinearLayoutManager(LinearLayout layout) {
        managedLinearLayout = layout;
    }

    public void addView(View view) {
        int count = managedLinearLayout.getChildCount();
        if (count == 0) {
            createChildLinearLayout();
        }
        LinearLayout activeChildLayout = (LinearLayout) managedLinearLayout.
                getChildAt(managedLinearLayout.getChildCount() -1);
        if (activeChildLayout.getChildCount() > 0) {
            View estimatedChild = activeChildLayout.getChildAt(0);
            int estimatedSize = activeChildLayout.getOrientation() == LinearLayout.VERTICAL ?
                    estimatedChild.getLayoutParams().height :
                    estimatedChild.getLayoutParams().width;
            int maxSize = activeChildLayout.getOrientation() == LinearLayout.VERTICAL ?
                    managedLinearLayout.getHeight() : managedLinearLayout.getWidth();
            if (estimatedSize * (activeChildLayout.getChildCount() + 1) > maxSize) {
                createChildLinearLayout();
                activeChildLayout = (LinearLayout) managedLinearLayout.
                        getChildAt(managedLinearLayout.getChildCount() -1);
            }
        }
        activeChildLayout.addView(view);
    }

    private void createChildLinearLayout() {
        LinearLayout childLayout = new LinearLayout(managedLinearLayout.getContext());
        int orientation = LinearLayout.VERTICAL;
        if (managedLinearLayout.getOrientation() == LinearLayout.VERTICAL) {
            orientation = LinearLayout.HORIZONTAL;
        }
        childLayout.setOrientation(orientation);
        int width = orientation == LinearLayout.VERTICAL ?
                LinearLayout.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
        int height = orientation == LinearLayout.VERTICAL ?
                LinearLayout.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        childLayout.setLayoutParams(params);
        managedLinearLayout.addView(childLayout);
    }
}
