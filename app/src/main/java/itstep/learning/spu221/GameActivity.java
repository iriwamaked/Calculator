package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    //размер поля 4 х 4
    private final int N=4;
    private final Random random=new Random();

    //массив, который будет содержать в себе числа
    private int[][] cells=new int[N][N];
    //представления ячеек
    private TextView[][] tVCells=new TextView[N][N];
    private Animation fadeInAnimation;
    private Animation scaleAnimation;
    private long score;
    private TextView tvScore;

    private List<Coord> movedCellsCoords = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        tvScore=findViewById(R.id.game_tv_score);

        Button newGame = findViewById(R.id.new_game);
        newGame.setOnClickListener(v-> startGame());

        //Обеспечение "квадратности" поля - одинаковости ширины и высоты
        LinearLayout gameField = findViewById(R.id.game_field);
        gameField.post(()->{
            //onCreate - событие, когда еще активность не "собрана" и ее размеры не известны
            //для того, чтобы узнать размеры необходимо создавать отложенные действия
            //с этой целью представления имеют метод post, который создает очередь действий,
            //которые выполняются когда представление становится "вмонтированным"

            //определяем актуальные размеры ширина окна gameField.width
            int w = this.getWindow().getDecorView().getWidth();
            int p = 2*3;
            //заменяем параметры layout на "квадратные"
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    w-p, //квадрат - одинаковая ширина
                    w-p  //и высота
            );
            layoutParams.setMargins(5,10,5,10);
            layoutParams.gravity= Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);
        });

        for (int i=0;i<N; i++){
            for(int j=0; j<N; j++){
                tVCells[i][j]=findViewById(
                        getResources()
                                .getIdentifier(
                                        ("game_cell_" + i) + j,
                                        "id",
                                        getPackageName()
                                )
                );
            }
        }

        int val=0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j]=val;
                //режим испытаний
                //if(val==0)val=2;
                //else val*=2;
            }
        }
        fadeInAnimation = AnimationUtils.loadAnimation( this, R.anim.faid_in);fadeInAnimation.reset();
        scaleAnimation = AnimationUtils.loadAnimation(this,R.anim.scale_game); scaleAnimation.reset();
        gameField.setOnTouchListener(
                new SwipeTouchListener(this){
                    @Override
                    public void onSwipBottom() {
                        if(moveBottom()){
                            spawnCell();
                        }
                        else{
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipLeft() {
                        if(moveLeft()){
                            spawnCell();
                        }
                        else{
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipRight() {
                        if(moveRight()){
                            spawnCell();
                        }
                        else{
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipTop() {
                        if(moveTop()){
                            spawnCell();
                        }
                        else{
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        startGame();
    }


    //13.	Делаем функцию, которая будет отображать числовой массив на массив ячеек
    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tVCells[i][j].setText(String.valueOf(cells[i][j]));
                int val = cells[i][j];
                if (val > 4096) val = 4096;
                tVCells[i][j].setTextAppearance(
                        getResources()
                                .getIdentifier(
                                        "game_cell_" + val,
                                        "style",
                                        getPackageName()
                                )
                );
                tVCells[i][j].setBackgroundColor(
                        getResources().getColor(
                                getResources()
                                        .getIdentifier(
                                                "game_digit_" + val,
                                                "color",
                                                getPackageName()
                                        ),
                                getTheme()
                        )
                );
            }
        }
    }
    private void startGame(){
        score=0;
        addScore(0);
        cells=new int[N][N];
        showField();
        spawnCell();
    }

    private void addScore(long value){
        score +=value;
        tvScore.setText(                            //заполнение ресурса-шаблона
                getString(                          //ресурс
                        R.string.game_score_tpl,    //данные для подстановки (по количеству % в шаблоне)
                        score)

        );
    }

    private boolean moveRight(){
        boolean wasMove=false;
        movedCellsCoords.clear();
        for (int i = 0; i < N; i++) {
            int pos1=-1; //позиция предыдущего числа
            for (int j = N-1; j >=0; j--) {
                if(cells[i][j]!=0){
                    if(pos1!=-1){ //раньше было найдено число левее
                        if(cells[i][pos1]==cells[i][j]){ //соединение
                            cells[i][pos1]+=cells[i][j];
                            cells[i][j]=0;
                            addScore(cells[i][pos1]);
                            movedCellsCoords.add(new Coord(i,pos1));
                            pos1=-1; //новый поиск, число слито
                            wasMove=true;

                        }
                        else //нашли другое число, новое число заменяем (меняем индекс)
                        {
                            pos1=j;
                        }
                    }
                    else{  //первое найденное число
                        pos1=j;
                    }
                }
            }
            //передвижение
            pos1=-1;
            for (int j = N-1; j >=0; j--) {
                if(cells[i][j]==0){
                    if(pos1==-1)pos1=j;
                }
                else{
                    if(pos1!=-1){
                        cells[i][pos1]=cells[i][j];
                        cells[i][j]=0;
                        addScore(cells[i][pos1]);
                        pos1--;
                        wasMove=true;
                    }
                }
            }
        }
        if (wasMove){
            showAnimatedCells();
        }
        return wasMove;
    }

    private boolean moveLeft(){
        boolean wasMove=false;
        movedCellsCoords.clear();
        for (int i = 0; i < N; i++) {
            int pos1=-1; //позиция предыдущего числа
            for (int j = 0; j < N; j++) {
                if(cells[i][j]!=0){
                    if(pos1!=-1){ //раньше было найдено число левее
                        if(cells[i][pos1]==cells[i][j]){ //соединение
                            cells[i][pos1]+=cells[i][j];
                            cells[i][j]=0;
                            addScore(cells[i][pos1]);
                            movedCellsCoords.add(new Coord(i,pos1));
                            pos1=-1; //новый поиск, число слито
                            wasMove=true;
                        }
                        else //нашли другое число, новое число заменяем (меняем индекс)
                        {
                            pos1=j;
                        }
                    }
                    else{  //первое найденное число
                        pos1=j;
                    }
                }
            }
            //передвижение
            pos1=-1;
            for (int j = 0; j < N; j++) {
                if(cells[i][j]==0){
                    if(pos1==-1)pos1=j;
                }
                else{
                    if(pos1!=-1){
                        cells[i][pos1]=cells[i][j];
                        cells[i][j]=0;
                        addScore(cells[i][pos1]);
                        pos1++;
                        wasMove=true;
                    }
                }
            }
        }
        if (wasMove){
            showAnimatedCells();
        }
        return wasMove;
    }


    private boolean moveTop() {
        movedCellsCoords.clear();
        boolean wasMove = false;
        for (int j = 0; j < N; j++) {
            int pos1 = -1; // позиция предыдущего числа
            for (int i = 0; i < N; i++) {
                if (cells[i][j] != 0) {
                    if (pos1 != -1) { // найдено число выше
                        if (cells[pos1][j] == cells[i][j]) { // объединение
                            cells[pos1][j] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[pos1][j]);
                            movedCellsCoords.add(new Coord(pos1,j));
                            pos1 = -1; // новый поиск, число слито
                            wasMove = true;
                        } else { // нашли другое число, изменяем индекс
                            pos1 = i;
                        }
                    } else {  // первое найденное число
                        pos1 = i;
                    }
                }
            }
            // передвижение
            pos1 = -1;
            for (int i = 0; i < N; i++) {
                if (cells[i][j] == 0) {
                    if (pos1 == -1) pos1 = i;
                } else {
                    if (pos1 != -1) {
                        cells[pos1][j] = cells[i][j];
                        cells[i][j] = 0;
                        pos1++;
                        wasMove = true;
                    }
                }
            }
        }
        if (wasMove){
            showAnimatedCells();
        }
        return wasMove;
    }

    private boolean moveBottom() {
        boolean wasMove = false;
        movedCellsCoords.clear();
        for (int j = 0; j < N; j++) {
            int pos1 = -1; // позиция предыдущего числа
            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] != 0) {
                    if (pos1 != -1) { // найдено число ниже
                        if (cells[pos1][j] == cells[i][j]) { // объединение
                            cells[pos1][j] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[pos1][j]);
                            movedCellsCoords.add(new Coord(pos1,j));
                            pos1 = -1; // новый поиск, число слито
                            wasMove = true;
                        } else { // нашли другое число, изменяем индекс
                            pos1 = i;
                        }
                    } else {  // первое найденное число
                        pos1 = i;
                    }
                }
            }
            // передвижение
            pos1 = -1;
            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] == 0) {
                    if (pos1 == -1) pos1 = i;
                } else {
                    if (pos1 != -1) {
                        cells[pos1][j] = cells[i][j];
                        cells[i][j] = 0;
                        pos1--;
                        wasMove = true;

                    }

                }

            }
        }
        if (wasMove){
            showAnimatedCells();
        }
        return wasMove;
    }
    /*
     * Появление нового числа с вероятностью
     * 0.9-2, 0.1-4
     * */
//    private boolean spawnCell(){
//        //собираем координаты все "пустых" ячеек (со значением) 0
//        List<Coord> coords = new ArrayList<>();
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                coords.add(new Coord(i,j));
//            }
//        }
//        if(coords.isEmpty()){    //нет возможности добавить число, поле заполнено (Game Over
//            return false;
//        }
//        //выбираем случайно одну из них,
//        int randomIndex=random.nextInt(coords.size());
//        Coord randomCoord = coords.get(randomIndex);
//        //задаем ей значение и анимацию появления
//        cells[randomCoord.i][randomCoord.j]=random.nextInt(10)==0?4:2;
//        tVCells[randomCoord.i][randomCoord.j].startAnimation( fadeInAnimation );
//        //перерисовываем поле
//        showField();
//        return true;
//
//    }

    /**
     * Поява нового числа
     * з імовірністю 0.9 - 2, 0.1 - 4
     */
    private boolean spawnCell() {
        // збираємо координати всіх "порожніх" комірок (зі значенням 0)
        List<Coord> coords = new ArrayList<>();
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                if( cells[i][j] == 0 ) {
                    coords.add( new Coord(i, j) );
                }
            }
        }
        if( coords.isEmpty() ) {  // немає можливості додати число - поле заповнене
            return false;
        }
        // вибираємо випадково одну з них
        int randomIndex = random.nextInt( coords.size() );
        Coord randomCoord = coords.get(randomIndex);

        // задаємо їх значення та анімацію появи
        cells[randomCoord.i][randomCoord.j] = random.nextInt( 10 ) == 0 ? 4 : 2;
        tVCells[randomCoord.i][randomCoord.j].startAnimation( fadeInAnimation );

        // перерисовуємо поле
        showField();
        return true;
    }
    static class Coord{
        int i;
        int j;
        public Coord(int i, int j){
            this.i=i;
            this.j=j;
        }
    }

    private void showAnimatedCells(){
        for (Coord coord: movedCellsCoords){
            tVCells[coord.i][coord.j].startAnimation(scaleAnimation);
        }
    }
}