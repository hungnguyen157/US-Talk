package com.example.ustalk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class BackgroundAwareLayout extends ConstraintLayout {
    private final Context context = this.getContext();
    private int childId;
    private View childView;
    private Paint eraser;
    private float radius;
    private RectF childRect;

    public BackgroundAwareLayout(Context context) throws Throwable {
        super(context);
        this.childRect = new RectF();
    }

    public BackgroundAwareLayout(Context context, AttributeSet attrs) throws Throwable {
        super(context, attrs);
        this.childRect = new RectF();
        this.setup(attrs);
    }

    public BackgroundAwareLayout(Context context, AttributeSet attrs, int defStyleAttr) throws Throwable {
        super(context, attrs, defStyleAttr);
        this.childRect = new RectF();
        this.setup(attrs);
    }

    private final void setup(AttributeSet attrs) throws Throwable {
        TypedArray ta = this.getContext().obtainStyledAttributes(attrs, R.styleable.BackgroundAwareLayout);
        childId = ta.getResourceId(R.styleable.BackgroundAwareLayout_child_id, 0);
        radius = (float)this.getResources().getDimensionPixelSize(R.dimen.onboarding_bubble_radius);
        if (childId != 0) {
            ta.recycle();
            this.setupEraser();
        } else {
            throw (Throwable)(new IllegalArgumentException("unable to find childId to create a hole"));
        }
    }

    public void onViewAdded(@NonNull View view) {
        super.onViewAdded(view);
        if (view.getId() == this.childId) {
            this.childView = view;
        }
    }

    private final void setupEraser() {
        eraser = new Paint();
        eraser.setColor(ContextCompat.getColor(this.getContext(), android.R.color.transparent));
        eraser.setXfermode((Xfermode)(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)));
        eraser.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_HARDWARE, (Paint)null);
    }

    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        childRect.set((float) childView.getLeft(), (float) childView.getTop(),
                (float) childView.getRight(), (float) childView.getBottom());
        canvas.drawRoundRect(childRect, radius, radius, eraser);
    }
}
