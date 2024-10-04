package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeTouchListener implements View.OnTouchListener{
    private  final GestureDetector gestureDetector;
    //context - как правило какой-то блок, на котором он действует (какой-то View)
    //область действия (может быть вся активность, а может быть ее часть)
    public SwipeTouchListener(Context context){
        //жестовый детектор определяет жесты, мы их обрабатываем
        gestureDetector=new GestureDetector(context,new SwipeGestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    //Outer interface - для реализации в коде
    public void onSwipBottom(){}
    public void onSwipLeft(){}
    public void onSwipRight(){}
    public void onSwipTop(){}

    //делаем сам Listener, есть более сложные детекторы (нам простого хватит)
    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        //накладываем ограничения на свайпы
        private static final int minDistance=70;
        private static final int minVelocity=100;//минимальная скорость

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            //начало жеста свайпа (swipe), true - значит, что мы обработали, если
            //наш детектор не сработал, значит передается дальше по
            return true;
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isHandled=false;
            if(e1!=null){
                float deltaX=e2.getX()-e1.getX();
                float deltaY=e2.getY()-e1.getY();
                //определить какого типа этот свайп (жест) -
                // вертикальный или горизонтальный (или никакой из них)
                //например, критерий - одна из дельт (по модулю) в два раза больше другой
                //Вычисляем абсолютные значения (без учета знака)
                float absX = Math.abs(deltaX);   // дельта может быть позитивной и негативной
                // если мы ведем сверху вниз - это негативный знак, если снизу вверх - позитивный
                //критерий "одна из дельт (по модулю) - по цифре, а не по знаку
                float absY = Math.abs(deltaY);
                if(absX>=2*absY){           //горизонтальный жест, значение имеет только velocityX
                    if(absX>=minDistance && Math.abs(velocityX)>=minVelocity){
                        if(deltaX>0){      //значит направление: e1.X   ------>   e2X
                            onSwipRight();
                        }
                        else {              //e1.X   <------   e2X
                            onSwipLeft();
                        }
                        isHandled=true;
                    }
                }
                else if(absY>=2*absX){      //вертикальный жест
                    if(absY>=minDistance && Math.abs(velocityY)>=minVelocity){
                        if(deltaX>0){      // e1.X
                            onSwipBottom(); //   |
                        }                  //   v
                        else {              //e2.X
                            onSwipTop();
                        }
                        isHandled=true;
                    }
                }
            }


            return isHandled;
        }
    }
}
