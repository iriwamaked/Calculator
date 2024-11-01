package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    //размер поля 4 х 4
    private final int N=4;
    private final Random random=new Random();
    private final String bestScoreFilename = "best_score.dat";

    //массив, который будет содержать в себе числа
    private int[][] cells=new int[N][N];

    private List<int[][]> cellsHistory = new ArrayList<>();
    private List<Long> scoreHistory = new ArrayList<>();
   // private int[][] cellsHistory=new int[N][N];
    //представления ячеек
    private TextView[][] tVCells=new TextView[N][N];
    //private TextView[][] tvCellsHistory=new TextView[N][N]
    private Animation fadeInAnimation;
    private Animation scaleAnimation;
    private long score;
    private TextView tvScore;
    private TextView tvBestScore;
    private long bestScore;


    private List<Coord> movedCellsCoords = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        tvScore=findViewById(R.id.game_tv_score);
        tvBestScore=findViewById(R.id.game_tv_best_score);
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoClick);
        findViewById(R.id.game_bnt_dialog).setOnClickListener(this::dialogClick);
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
        addScore(-score);
        loadMaxScore();
        showMaxScore();
        cells=new int[N][N];
        showField();
        spawnCell();
    }

    private void saveCurrentState() {
        int[][] currentState = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, currentState[i], 0, N);
        }
        cellsHistory.add(currentState);
        scoreHistory.add(score);
    }

    private void undoClick(View view){
        if (!cellsHistory.isEmpty()) {
            cells = cellsHistory.remove(cellsHistory.size() - 1);
            score=scoreHistory.remove(scoreHistory.size() - 1);
            tvScore.setText(
                    getString(R.string.game_score_tpl, score)

            );
            showField(); // Обновляем отображение
        } else {
            Toast.makeText(this, "Нет доступных ходов для отмены", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("PrivateResource")
    private void dialogClick(View view){
        String[] items = {"Item 1", "Item 2", "Item 3"}; // Массив элементов
        boolean[] checkedItems = {false, false, false};
        //Диалоги
        /*
        new AlertDialog.Builder(this)
//                com.google.android.material.R.style
//                        //.Base_ThemeOverlay_AppCompat_Dialog_Alert)
//                        .Base_Theme_AppCompat_Dialog_Alert)
              //  .setTitle("Dialog example")
                .setMessage("Приклад модального ділогу")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .setPositiveButton("Закрити", (dialog, which)->{})
                .setNegativeButton("Вихід", (dialog, which)->this.finish())
                .setNeutralButton("Нова гра", (dialog, which)->this.startGame())
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    // Обработчик выбора элемента
                    checkedItems[which] = isChecked;})
                .show();*/
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Dialog example")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setCancelable(false)
                    .setPositiveButton("Закрити", (dialog, which) -> {})
                    .setNegativeButton("Вихід", (dialog, which) -> this.finish())
                    .setNeutralButton("Нова гра", (dialog, which) -> this.startGame())
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                        checkedItems[which] = isChecked;
                    })
                    .show();
        });
    }

    private void saveMaxScore(){
        /* Работа с файлами
        У Андроид работа с файлами делится на две категории:
        - "частные" файлы - файлы с репозитория приложения, которые удаляются
            при удалении приложения.
            Когда заходим в настройки приложения, видим, что приложение занимает места 20 мб +
            его данные занимают 30 мб. К папке приложения доступ неограниченный, но это можно
            просто удалить из настроек приложения и оно же удаляется при удалении всего приложения
       - общие файлы - из других репозиториев "фото/галерея", "загрузки" и т.д. Эти файлы не удаляются
            из приложения, они с ним не связываются. Если удалить приложение, то эти файлы останутся
            и для доступа к этим файлам требуется разрешение.
        * */
        //расширения в имени файла не имеет значения. Это просто сохраниение. Кроме того,
        // Android ближе к Linux, для которых понятие расширения вообще не базовое, можно вообще ничего не писать
        try (FileOutputStream fos = openFileOutput(
                bestScoreFilename,
                Context.MODE_PRIVATE);
            DataOutputStream writer = new DataOutputStream(fos)){
                writer.writeLong(bestScore);
                writer.flush();
        }
        catch (IOException ex){
            Log.e("saveMaxScore","fos: " + ex.getMessage());
        }
    }
    private void loadMaxScore(){
        try (FileInputStream fis = openFileInput(bestScoreFilename);
             DataInputStream reader = new DataInputStream(fis)) {
            bestScore = reader.readLong();
        }
        catch (IOException ex){
            Log.e("loadMaxScore", "fis" + ex.getMessage());
        }
    }

    private void showMaxScore(){
        tvBestScore.setText(
                getString(R.string.game_best_score_tpl, bestScore)

        );
    }

    private void addScore(long value){
        score +=value;
        tvScore.setText(
                getString(R.string.game_score_tpl, score)

        );
        if (score>bestScore){
            bestScore=score;
            saveMaxScore();
            showMaxScore();
        }
    }

    private boolean moveRight(){
        boolean wasMove=false;
        movedCellsCoords.clear();
        saveCurrentState();
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
        saveCurrentState();
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
        saveCurrentState();
        boolean wasMove = false;
        for (int j = 0; j < N; j++) {
            int pos1 = -1; // позиция предыдущего числа
            for (int i = 0; i < N; i++) {
                if (cells[i][j] != 0) {
                    System.out.println("Текущая ячейка: (" + i + "," + j + ") со значением " + cells[i][j]);
                    if (pos1 != -1) { // найдено число выше
                        if (cells[pos1][j] == cells[i][j]) { // объединение
                            cells[pos1][j] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[pos1][j]);
                            movedCellsCoords.add(new Coord(pos1,j));
                            System.out.println("Объединение ячеек (" + pos1 + "," + j + ") и (" + i + "," + j + ")");
                            pos1=i;
                            wasMove = true;
                        } else { // нашли другое число, изменяем индекс
                            pos1 = i;
                            System.out.println("Новое значение pos1: " + pos1);
                        }
                    } else {  // первое найденное число
                        pos1 = i;
                        System.out.println("Первое найденное число, pos1: " + pos1);
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

                        wasMove = true;
                        pos1++;
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
        saveCurrentState();
        for (int j = 0; j < N; j++) {
            int pos1 = -1; // позиция предыдущего числа
            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] != 0) {
                    System.out.println("Текущая ячейка: (" + i + "," + j + ") со значением " + cells[i][j]);
                    if (pos1 != -1) { // найдено число ниже
                        if (cells[pos1][j] == cells[i][j]) { // объединение
                            cells[pos1][j] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[pos1][j]);
                            movedCellsCoords.add(new Coord(pos1,j));
                            System.out.println("Объединение ячеек (" + pos1 + "," + j + ") и (" + i + "," + j + ")");

                            wasMove = true;
                            if (pos1 >= 1) pos1--; // новый поиск, число слито
                        } else { // нашли другое число, изменяем индекс
                            pos1 = i;
                            System.out.println("Новое значение pos1: " + pos1);
                        }
                    } else {  // первое найденное число
                        pos1 = i;
                        System.out.println("Первое найденное число, pos1: " + pos1);
                    }

                }
                // Проверка границ массива
                if (pos1 < 0 || pos1 >= N || i < 0 || i >= N) {
                    Log.d("moveBottom", "Индекс вышел за границы массива: i=" + i + ", pos1=" + pos1);
                    continue;
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
                        if (pos1 >= 1) pos1--;
                        wasMove = true;
                        if ( pos1 < N && i < N) {
                            Log.d("moveBottom", "Перемещение из (" + i + "," + j + ") в (" + (pos1 + 1) + "," + j + ")");
                        }
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