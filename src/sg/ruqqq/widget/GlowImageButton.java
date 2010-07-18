package sg.ruqqq.widget;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class GlowImageButton extends ImageButton {
	public GlowImageButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}
	
	public GlowImageButton(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	public GlowImageButton(Context context) {
	    super(context);
	}
	
	boolean drawGlow = false;
	//this is the paint object which specifies the color and alpha level 
	//of the circle we draw
	Paint paint = new Paint();
	{
	    paint.setAntiAlias(true);
	    paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
	    paint.setColor(Color.WHITE);
	    paint.setAlpha(50);
	};

	@Override
	public void draw(Canvas canvas){
	    super.draw(canvas);
	    if(drawGlow)
	        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
	    if(event.getAction() == MotionEvent.ACTION_DOWN) {
	        drawGlow = true;
	    } else if(event.getAction() == MotionEvent.ACTION_UP) {
	        drawGlow = false;
		}
	    
	    this.invalidate();
	    return super.onTouchEvent(event);
	}
}
