package itstep.learning.spu221;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;
    private String zeroSign;
    private final int maxDigits=11;
    private String dotSign;
    private String minusSign;
    private boolean needClearResult; //стирает при вводе - после операции
    private String currentOperation=null;
    private boolean needClearHistory;
    private double firstDigit=0;
    private String currentDigit=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calc_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHistory=findViewById(R.id.calc_tv_history);
        tvResult=findViewById(R.id.calc_tv_result);
        zeroSign=getString(R.string.calc_btn_digit_0);
        dotSign=getString(R.string.calc_btn_dot);
        minusSign=getString(R.string.calc_minus_sign);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::dotClick);
        findViewById(R.id.calc_btn_sign_toggle).setOnClickListener(this::pmClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::sqClick);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::oneDevideX);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::sqrtClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::equalClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::percentClick);
        for(int i=0; i<10; i++){
            findViewById(
                    //R.id.calc_btn_c
                    getResources()   //R
                    .getIdentifier(
                            "calc_btn_digit_" + i,
                            "id", //.id
                            getPackageName()
                    )
            ).setOnClickListener(this::digitClick);
        }
        if(savedInstanceState==null) {   //если это первый запуск, а не смена конфигурации
            //делаем иммитацию того, что произошло событие clearClick
            this.clearClick(null);
            needClearResult=false;
            needClearHistory=false;
        }
    }

    //при изменении конфигурации устройства происходит пересборка активности,
    //из-за чего осуществляется перезапуск  и исчезают данные, которые наработаны
    //для того, чтобы их сохранить, необходимо использовать события жизненного цикла активности
    //onSaveInstanceState и onRestoreInstanceState
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState - Map, которое сохраняет разные типы данных по принципу ключ-значение
        // тексты из вьющек приходят как CharSequence, поэтому мы можем и сохранить его в том же виде
        outState.putCharSequence("savedResult", tvResult.getText());
        outState.putBoolean("needClearResult", needClearResult);
        outState.putBoolean("needClearHistory", needClearHistory);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //savedInstanceState - ссылка на outState, которых сохранялся в SaveInstanceState
        tvResult.setText(savedInstanceState.getCharSequence("savedResult"));
        needClearResult=savedInstanceState.getBoolean("needClearResult");
        needClearHistory=savedInstanceState.getBoolean("needClearHistory");
    }

    private void clearClick(View view){
        tvHistory.setText("");
        tvResult.setText(zeroSign);
    }

    private void digitClick(View view){
        //.getText() возвращает интерфейс CharSequence
        String res=needClearResult?"":tvResult.getText().toString();
        String currentHistory=needClearHistory?"":tvHistory.getText().toString();
        needClearResult=false;
        needClearHistory=false;
        if(digitLength(res)>=maxDigits){
            //toаst - выскакивающее сообщение
            Toast.makeText(this, R.string.calc_msg_max_digits,Toast.LENGTH_SHORT).show();
            return;
        }
        //пишем гарантированно от не null
        // (если сделать res.equals("0") -нет гарантии, что в res!=null
        //кроме того, цифру надо достать программно, иначе 0 вообще не будет нажиматься
        if(zeroSign.equals(res)){
            //если стоит на старте 0, и мы нажимаем какую-то цифру - затирает 0
            res="";
        }
        //view - очень общий тип, поэтому, чтобы взять текст, приводим к Button
        //не каждая view может вывести тип
        res+=((Button)view).getText();
        currentDigit=res;
        tvResult.setText(res);
    }

    private void dotClick(View view){
        String res=tvResult.getText().toString();
        if(res.contains(dotSign)){
            if(res.endsWith(dotSign)){
                res=res.substring(0,res.length()-1);
            }
            else {
                //LENGTH_SHORT - длительность времени показа сообщения
                Toast.makeText(this, R.string.calc_msg_two_dots, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else{
            res+=dotSign;

        }
        tvResult.setText(res);
    }

    private void pmClick(View view){
        String res=tvResult.getText().toString();
        if(zeroSign.equals(res)){
            Toast.makeText(this, R.string.calc_msg_minus_zero, Toast.LENGTH_SHORT).show();
            return;
        }
        if(res.startsWith(minusSign)){
            res=res.substring(minusSign.length());
        }
        else{
            res=minusSign+res;
        }
        tvResult.setText(res);
    }

    private void backspaceClick(View view){
        String res=tvResult.getText().toString();
        int len=res.length();
        if(len>1){
            res=res.substring(0,len-1);
            if(minusSign.equals(res)){
                res=zeroSign;
            }
        }
        else{
            res=zeroSign;
        }
        tvResult.setText(res);
    }

    private int digitLength(String input){
        //длина результата в цифрах (без знака "-" и ".")
        int ret=0;
        ret=input.length();
        if(input.startsWith(minusSign)){
            ret-=1;
        }
        if(input.contains(dotSign)){
            ret--;
        }
        return ret;
    }

    private void sqClick(View view)
    {
        String res=tvResult.getText().toString();
        //если так написать, то получим исключение,
        //потому что 0, запятая, минус, точка - не классические цифры
        //что приведет к ошибки Parse
        //double x = Double.parseDouble(res);
        //правильно
        double x=parseX(res);
        needClearResult=true;
        needClearHistory=true;
        String currentHistory = tvHistory.getText().toString();
        tvHistory.setText(currentHistory+"x²(" + res + ") ");
        showResult(x*x);

    }

    private void oneDevideX(View view){
        String res=tvResult.getText().toString();
        double x = parseX(res);
        needClearResult=true;
        needClearHistory=true;
        String currentHistory = tvHistory.getText().toString();
        tvHistory.setText(currentHistory+"√(" + res + ")");
        showResult(1/x);
    }

    private void sqrtClick(View view){
        String res=tvResult.getText().toString();
        double x = parseX(res);
        if (x < 0) {
            Toast.makeText(this, R.string.calc_msg_negative_sqrt, Toast.LENGTH_SHORT).show();
            return;  // Выход, если значение отрицательное
        }
        String currentHistory = tvHistory.getText().toString();
        currentDigit="";
//        Log.d("CalcActivity", "Current Digit: " + currentDigit);
//
//
//        Log.d("CalcActivity", "Current History: " + currentHistory);
        tvHistory.setText(currentHistory+"√(" + res + ")");
        needClearResult=true;
        needClearHistory=true;
        showResult(Math.sqrt(x));
    }

    private void operationClick(View view){
        String res=tvResult.getText().toString();
        firstDigit= parseX(res);
        int viewId=view.getId();
        if(viewId == R.id.calc_btn_divide) {
            currentOperation = "/";
        }
        else if(viewId==R.id.calc_btn_plus){
                currentOperation="+";}
        else if(viewId==R.id.calc_btn_multiply){
                currentOperation="*";}
        else if(viewId==R.id.calc_btn_minus){
                currentOperation="-";
        }

        String currentHistory = tvHistory.getText().toString();
        tvHistory.setText(currentHistory+" " + currentDigit +" " + currentOperation + " " );
        currentDigit=null;
        needClearResult=true;

    }

    private void equalClick(View view){
        String res=tvResult.getText().toString();
        double secondDigit=parseX(res);
        double result=0;
        switch (currentOperation){
            case "/":
                if (secondDigit!=0)
                {
                    result=firstDigit/secondDigit;
                }
               else{
                    Toast.makeText(this, R.string.calc_msg_divide_by_zero, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "+":
                result=firstDigit+secondDigit;
                break;
            case "-":
                result=firstDigit-secondDigit;
                break;
            case "*":
                result=firstDigit*secondDigit;
                break;
        }

        tvHistory.setText(Double.toString(result));
        needClearResult=true;
        needClearHistory=true;
        showResult(result);
    }

    private void percentClick(View view) {
        String res = tvResult.getText().toString();
        double currentValue = parseX(res);  // Текущее число на экране

        if (currentOperation == null) {
            // Если нет текущей операции, просто вычисляем процент от текущего числа
            showResult(currentValue / 100);
        } else {
            // Если операция есть, вычисляем процент как часть текущей операции
            // Например, для "200 + 10%" считаем 10% от 200 и добавляем
            double result = 0.0;
            switch (currentOperation) {
                case "+":
                    result = firstDigit + (firstDigit * currentValue / 100);
                    break;
                case "-":
                    result = firstDigit - (firstDigit * currentValue / 100);
                    break;
                case "*":
                    result = firstDigit * (currentValue / 100);
                    break;
                case "/":
                    if (currentValue != 0) {
                        result = firstDigit / (currentValue / 100);
                    } else {
                        // Обработка деления на ноль
                        Toast.makeText(this, R.string.calc_msg_divide_by_zero, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
            }
            showResult(result);
        }
    }
    private Double parseX(String res){
        //если так написать, то получим исключение,
        //потому что 0, запятая, минус, точка - не классические цифры
        //что приведет к ошибки Parse
        //double x = Double.parseDouble(res);
        //правильно
        res=res
                .replace(zeroSign, "0")
                .replace(minusSign,"-")
                .replace(dotSign, ".");
        //если и для других цифр используем другие спецсимволы - и для них тоже надо это сделать
        //только после приводим
        double x=Double.parseDouble(res);
        return x;
    }

    private void showResult(double x){
        if(x>=1e10||x<=-1e10){
            //TODO: перенести до ресурсів повідомлення
            tvResult.setText(R.string.calc_msg_overflow);
            return;
        }
        //у примитивных типов toString не определен
        String res=x==(int)x
                ?String.valueOf((int)x)
                :String.valueOf(x);

        //теперь делаем все с точностью да наборот
        res=res
                .replace("0", zeroSign)
                .replace("-", minusSign)
                .replace(".", dotSign);
        int limit=maxDigits;
        if (res.startsWith(minusSign)){
            limit+=1;
        }
        if(res.contains(dotSign)){
            limit+=1;
        }
        if(res.length()>limit){
            res=res.substring(0,limit);
        }
        //выводим текст с результатом
        tvResult.setText(res);
    }

}