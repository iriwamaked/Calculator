package itstep.learning.spu221;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RatesActivity extends AppCompatActivity {
    private final static String nbuUrl="https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private LinearLayout ratesContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //находим по айдишнику наш LinearLayout и когда он будет готов, постим в него курсы валют
        ratesContainer= findViewById(R.id.rates_ll_container);
        ratesContainer.post (()-> new Thread(this::loadRates).start());
    }

    private void loadRates(){
        try {
            //аналогично FILE объект не делает ничего, кроме создания
            //(не подключается и т.д.)
            URL url = new URL (nbuUrl);
            //Когда нам нужно к нему обратиться
            InputStream urlStream = url.openStream();
            /*Подключение может сопровождаться следущими проблемами:
            - android.os.NetworkOnMainThreadException - в Андроид подключение к сети не может
               осуществляться в том же потоке, в котором работает UI (User Interface). Это запрещено
               на уровне системы. В других системах, как правило, это не запрещено, а тут запрещено
               Это связано с тем, что работа с сетью - это долго
               Что делать? - post - открываем новый поток
            - java.lang.SecurityException - для доступа в Интернет нужно заявить в манифесте
            - java.lang.NullPointerException: Can't toast on a thread that has not called Looper.prepare()
               запущенные в другом потоке, чем UI, действия не могут иметь доступ к UI,
               в том числе, Toast. Или к самим элементам, или вызвать Toast, а выходит,
               что this - не совсем то, о чем мы думаем. Соответственно, он пытается Toast
               от имени того потока, но он не является интерфейсным, это просто поток кода
               и к нему нельзя привязать Toast, который идет от интерфеса, от окна, от активности,
               но не от какого-то new Thread
               Запуск таких действий следует делегировать UI потоку, для чего есть специальный инструмент

             */
            String jsonString = readAsString(urlStream);

            runOnUiThread(()-> showRates(jsonString));

            urlStream.close();

        }
        catch (MalformedURLException ex){
            Log.d("loadRates", "MalformedURLExceprion" + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadRates", "IOException" + ex.getMessage());
        }
        catch (android.os.NetworkOnMainThreadException ex){
            Log.d("loadRates", "NetworkOnMainThreadException" + ex.getMessage());
        }
        catch (SecurityException ex){
            Log.d("loadRates", "SecurityException" + ex.getMessage());
        }
    }

    private void showRates(String jsonString){
        try {
            JSONArray ratesJsonArray = new JSONArray(jsonString);
            ArrayList<JSONObject> ratesList = new ArrayList<>();

            // Заполняем список JSON объектами из массива
            for (int i = 0; i < ratesJsonArray.length(); i++) {
                ratesList.add(ratesJsonArray.getJSONObject(i));
            }

            // Сортируем список по названию валюты (поле "txt")
            Collections.sort(ratesList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    try {
                        return o1.getString("txt").compareToIgnoreCase(o2.getString("txt"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            // Отображаем отсортированные данные
            for (JSONObject rate : ratesList) {
                String currencyName = rate.getString("txt");
                double exchangeRate = rate.getDouble("rate");

                // Создаем горизонтальный контейнер для двух текстовых полей
                LinearLayout rateItemLayout = new LinearLayout(this);
                rateItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                rateItemLayout.setPadding(10, 5, 10, 5);
                rateItemLayout.setBackground(AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.calc_btn_equal));

                // Настраиваем параметры для внешнего отступа
                LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                itemLayoutParams.setMargins(10, 5, 10, 5);
                rateItemLayout.setLayoutParams(itemLayoutParams);

                // Создаем текстовое поле для названия валюты
                TextView currencyNameTextView = new TextView(this);
                currencyNameTextView.setText(currencyName);
                currencyNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                // Создаем текстовое поле для курса валюты с выравниванием по правому краю
                TextView exchangeRateTextView = new TextView(this);
                exchangeRateTextView.setText(String.format("%.4f", exchangeRate));
                exchangeRateTextView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
                exchangeRateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Добавляем текстовые поля в контейнер
                rateItemLayout.addView(currencyNameTextView);
                rateItemLayout.addView(exchangeRateTextView);

                // Добавляем контейнер в основной контейнер для курсов
                ratesContainer.addView(rateItemLayout);
            }
        } catch (JSONException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        /*JSONArray rates;
        try {
            rates = new JSONArray(jsonString);
            //парсим JSON, изымаем только текст,
            // делаем из него вью и закидываем на контейнер
            for (int i = 0; i < rates.length(); i++) {
                JSONObject rate = rates.getJSONObject(i);
                String currencyName = rate.getString("txt");
                double exchangeRate = rate.getDouble("rate");

                /*TextView tv = new TextView(RatesActivity.this);
                tv.setText(currencyName+ ": " + exchangeRate);
                tv.setPadding(10,5,10,5);
                tv.setBackground(AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.calc_btn_equal));
                LinearLayout rateItem = new LinearLayout(RatesActivity.this);
                rateItem.setOrientation(LinearLayout.HORIZONTAL);
                rateItem.setPadding(10,5,10,5);
                rateItem.setBackground(AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.calc_btn_equal));

                //Параментры для внешнего отсутпа
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                        //Параметры, как этот элемент будет вставляться во внешние элементы
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(10,5,10,5);
                rateItem.setLayoutParams(lp);

                // Создаем текстовое поле для названия валюты
                TextView currencyNameTextView = new TextView(this);
                currencyNameTextView.setText(currencyName);
                currencyNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); // Вес 1 для распределения пространства

                // Создаем текстовое поле для курса валюты с выравниванием по правому краю
                TextView exchangeRateTextView = new TextView(this);
                exchangeRateTextView.setText(String.format("%.4f", exchangeRate));
                exchangeRateTextView.setGravity(View.TEXT_ALIGNMENT_TEXT_END); // Выравнивание по правому краю
                exchangeRateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Добавляем текстовые поля в контейнер
                rateItem.addView(currencyNameTextView);
                rateItem.addView(exchangeRateTextView);


                ratesContainer.addView(rateItem);


            }
        } catch (JSONException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }*/
    }

    /*поскольку из InputStream может быть исколючение, мы его добавляем к методу
    //try catch не пишем, потому что InputStream приходим к нас наружи
    //и если мы не можем его читать, то такие решения мы в программе не принимаем
    //если бы мы этот Stream открывали прямо тут, мы бы еще могли что-то делать
    //мы его открываем извне, поэтому тут мы можем только выкинуть исключение,
    //что не могли мы считать ваш Stream, все остальное у нас уже безопасно*/
    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuilder=new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while((len=inputStream.read(buffer))>0){
            //фиксируем, переносим полученные байты в buildBuilder от нуля
            //до того, сколько мы на самом деле прочитали
            byteBuilder.write(buffer, 0, len);
        }
        //возвращаем String
        return byteBuilder.toString();
    }
}
