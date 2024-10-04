package itstep.learning.spu221;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {
    private Animation fadeInAnimation;
    private Animation scale1Animation;
    private Animation scale2Animation;
    private Animation rotate1Animation;
    private Animation trans1Animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //загружаем анимацию
        fadeInAnimation= AnimationUtils.loadAnimation(this,R.anim.faid_in);
        //после загрузки анимации желательно дать ей reset() по рекомендации Google
        //работает и без этого
        fadeInAnimation.reset();

        scale1Animation=AnimationUtils.loadAnimation(this, R.anim.scale_1);
        scale1Animation.reset();
        scale2Animation=AnimationUtils.loadAnimation(this, R.anim.scale_2);
        scale2Animation.reset();
        rotate1Animation=AnimationUtils.loadAnimation(this, R.anim.rotate1);
        rotate1Animation.reset();
        trans1Animation=AnimationUtils.loadAnimation(this, R.anim.trans_1);
        trans1Animation.reset();
        findViewById(R.id.anim_v_alpha).setOnClickListener(this::alphaClick);
        findViewById(R.id.anim_v_scale1).setOnClickListener(this::scale1Click);
        findViewById(R.id.anim_v_scale2).setOnClickListener(this::scale2Click);
        findViewById(R.id.anim_v_rotate1).setOnClickListener(this::rotate1Click);
        findViewById(R.id.anim_v_trans_1).setOnClickListener(this::trans1Click);
    }

    private void alphaClick(View view){
        //проигрываем анимацию на представлении
        view.startAnimation(fadeInAnimation);
    }
    private void scale1Click(View view){
        //проигрываем анимацию на представлении
        view.startAnimation(scale1Animation);
    }

    private void scale2Click(View view){
        //проигрываем анимацию на представлении
        view.startAnimation(scale2Animation);
    }

    private void rotate1Click(View view){
        //проигрываем анимацию на представлении
        view.startAnimation(rotate1Animation);
    }

    private void trans1Click(View view){
        //проигрываем анимацию на представлении
        AnimationSet animationSet=new AnimationSet(true);
        animationSet.addAnimation(trans1Animation);
        animationSet.addAnimation(scale2Animation);
        animationSet.setRepeatCount(-1);
        animationSet.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animationSet);
    }
}