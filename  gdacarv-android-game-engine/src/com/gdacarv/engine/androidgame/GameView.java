package com.gdacarv.engine.androidgame;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class GameView extends SurfaceView{
	
	private SurfaceHolder mHolder;
	protected GameLoopThread gameLoopThread;
	protected ArrayList<Sprite> mSprites;
	protected HandlerTouchEvents mHandlerTouchEvents;
	
	public long FPS = 30;
	
	public GameView(Context context, boolean handler) {
		super(context);
		if(handler)
			mHandlerTouchEvents = new HandlerTouchEvents();
		mSprites = new ArrayList<Sprite>();
		gameLoopThread = new GameLoopThread(this);
		mHolder = getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {

               @Override
               public void surfaceDestroyed(SurfaceHolder holder) {
            	   boolean retry = true;
                   gameLoopThread.setRunning(false);
                   while (retry) {
                          try {
                                gameLoopThread.join();
                                retry = false;
                          } catch (InterruptedException e) {
                          }
                   }
               }

               @Override
               public void surfaceCreated(SurfaceHolder holder) {
            	   if(gameLoopThread.getState() == Thread.State.TERMINATED)
            		   gameLoopThread = new GameLoopThread(GameView.this);
            	   else
                	   onLoad();            		   
            	   gameLoopThread.setRunning(true);
                   gameLoopThread.start();
               }

               @Override
               public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
               }
        });
	}

	protected abstract void onLoad();

	@Override
    protected void onDraw(Canvas canvas) {
		canvas.drawRGB(0, 0, 0);
		for (Sprite sprite : mSprites) 
            sprite.onDraw(canvas);
    }
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {
			TouchEvents(event);
			if(mHandlerTouchEvents != null){
				mHandlerTouchEvents.handle(event);
			}
		}
		return true;
	}
	
	public void TouchEvents(MotionEvent event) {
	}
	
	public void TouchEvents(HandlerTouchEvents handler) {
	}

	public void input() {
		if(mHandlerTouchEvents != null){
			TouchEvents(mHandlerTouchEvents);
		}
	}
	
	public void update() {
		for (Sprite sprite : mSprites) 
            sprite.update();
	}

	public class HandlerTouchEvents {
		
		public int[] x = {0,0}, y = {0,0};		
		public int[] xLast = {0,0}, yLast = {0,0};
		public boolean[] touching = {false, false};
		

		public void handle(MotionEvent event) {
			int xTemp, yTemp;
			updatePosition(0, (int) event.getX(0), (int) event.getY(0));
			xTemp = (int) event.getX(1);
			yTemp = (int) event.getY(1);
			if(xTemp != x[0] || yTemp != y[0]){
				updatePosition(1, xTemp, yTemp);
				touching[1] = true;
			}
			else{
				touching[1] = false;
			}
			if(event.getAction() == 0)
				touching[0] = true;
			else if(event.getAction() == 1)
				touching[0] = false;
		}
		
		public void updatePosition(int index, int xNew, int yNew){
			if(x[index] != xNew || y[index] != yNew){
				xLast[index] = x[index];
				x[index] = xNew;
				yLast[index] = y[index];
				y[index] = yNew;
			}
		}
		
	}
}