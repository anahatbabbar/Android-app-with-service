package com.example.android.sunshine1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Anahat.Babbar on 1/4/2015.
 */

/*
    Anahat - This class is just a sample class to understand HOW CUSTOM VIEWS WORK. It is NOT being used in app anywhere though.
*/

public class MyView extends View{

    //Anahat - Constructor overloaded if the view is called from the code.
    public MyView(Context context) {
        super(context);
    }

    //Anahat - Cnstructor overloaded if the view is called form the XML with attributes passed in.
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //Anahat - Constructor overloaded if the view is called by the inflator
    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Anahat - onMeasure method is called by the parent view to ask for how much space the custom view being built needs for display.
    // Anahat - The parent passes in its size values, i.e. height and width.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Anahat - Get the mode and size for height from parent. Mode can be Exact or Wrap Text etc
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;
        if (hSpecMode == MeasureSpec.EXACTLY){
            myHeight = hSpecSize;
        }
        else if (hSpecMode == MeasureSpec.AT_MOST){
            //Wrap Content. How ???
        }

        // Anahat - Get the mode and size for width from parent. Mode can be Exact or Wrap Text etc
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;
        if (wSpecMode == MeasureSpec.EXACTLY){
            myWidth = wSpecSize;
        }
        else if (wSpecMode == MeasureSpec.AT_MOST){
            //Wrap Content. How ???
        }

        // Anahat - Setting the dimensions of the custom view now.
        setMeasuredDimension(myWidth, myHeight);

    }

    // Anahat - This overridden method is called to draw the custom view. Canvas object passed in as the parameter contains all methods to draw, redraw views.
    // Anahat - VERY IMPORTANT. THIS METHOD IS TYPICALLY CALLED MULTIPLE TIMES A SECOND TO DRAW AND REDRAW OBJECTS.
    // Anahat - THUS, DECLARE ALL OBJECTS AND VARIABLES USED HERE AS CLASS VARIABLES SO THAT GARBAGE COLLECTION DOES NOT AFFECT THE PERFORMANCE.
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}







