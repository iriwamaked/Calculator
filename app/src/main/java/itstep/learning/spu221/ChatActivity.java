package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private final static String chatUrl = "https://chat.momentfor.fun/";
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private final List<ChatMessage> messages = new ArrayList<>();
    //Android.os Handler - позволяет отправлять сообщения в процессе, ставить их в очередь
    //для отложенных запусков
    private final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
       // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
       //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //    return insets;
        //});
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendMessageClick);
        etAuthor= findViewById(R.id.chat_et_nik);
        etMessage=findViewById(R.id.chat_et_message);
        chatContainer= findViewById(R.id.chat_ll_container);
        chatScroller=findViewById(R.id.chat_sv_container);
        handler.post (this::reloadChat);
    }

    private void reloadChat(){
        new Thread(this::loadChat).start();
        handler.postDelayed(this::reloadChat, 3000);
    }
    private void onSendMessageClick(View view){
        String author = etAuthor.getText().toString();
        if(author.isBlank()){
            Toast.makeText(this, "Ввведіть нік", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if(message.isBlank()){
            Toast.makeText(this, "Ввведіть повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(()->sendMessage(new ChatMessage(author, message))).start();
}

    private void sendMessage(ChatMessage chatMessage){
        /*Для того, чтобы отправить новое сообщение нужно отправтиь запрос методом POST
        с данными, которые имитируют отправку формы с параметрами author и msg
        - метод POST
        - заголовок формы Content-Type: application/x-www-form-urlencoded
        - тело в форме author=The%20Author&msg=Hello%20All,
           где данные прошли URL-кодировку(например, %20 - пробел, знак равно, амперсенд)
           при получении назад - надо не забыть декодирование
        Не забываем про перекодировку символов
        * */
        try {
            //напоминаем, что тут урл не открывается, просто создается объект
            URL url = new URL (chatUrl);
            //открываем соединение
           HttpURLConnection connection=(HttpURLConnection) url.openConnection();
           //настриваем соединение
            connection.setDoInput(true); //c соединения можно читать (ожидается ответ)
            connection.setDoOutput(true); //запись - добавление тела запроса
            //сам запрос, общие настройки
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");   //заголовки, отправляем форму
            connection.setRequestProperty("Accept-Type", "application/json");    //то, что мы готовы принять
            connection.setChunkedStreamingMode(0); //не делить на части (chunk)
            //заполняем тело
            OutputStream bodyStream = connection.getOutputStream();
            String body = String.format(
                    "author=%s&msg=%s",
                    URLEncoder.encode((chatMessage.getAuthor()), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode((chatMessage.getText()), StandardCharsets.UTF_8.name())
            );
            bodyStream.write(body.getBytes(StandardCharsets.UTF_8));
            bodyStream.flush(); //отправка - "выталкивание" буфера в канал
            bodyStream.close();
            //получаем ответ
            int statusCode = connection.getResponseCode();
            if (statusCode==201){   //сообщение принято сервером
                //запускаем обновление сообщений
                loadChat();
            }
            else{  //ошибка приема сообщения
                //изымаем тело ответа, но из Error-канала
                InputStream errorStream= connection.getErrorStream();
                String errorMessage = readAsString(errorStream);
                runOnUiThread(()->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show());
                errorStream.close();
            }
            //разрываем соединение
            connection.disconnect();
        }
        catch (Exception ex)
        {
            Log.e("sendChat", ex.getMessage());
        }

    }

    private void showChat(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            //преобразовываем объект, изымаем из него данные
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //chatContainer.removeAllViews();
            boolean wasNewMessage = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                ChatMessage chatMessage = new ChatMessage(jsonArray.getJSONObject(i));
                //проверяем, есть ли это сообщение в ранее принятых (во внутренней коллекции)
                if (this.messages.stream().noneMatch(m -> m.getId().equals(chatMessage.getId()))) {
                    //новое сообщение - добавляем в коллекцию
                    messages.add(chatMessage);
                    wasNewMessage = true;
                }
            }
            if(wasNewMessage){
                messages.sort((m1,m2)-> m1.getMoment().compareTo(m2.getMoment()));
                //chatContainer.removeAllViews();
                for (ChatMessage chatMessage: messages) {
                    if(chatMessage.getView()==null){ //сообщения не отображены
                        chatMessage.setView(messageView(chatMessage));
                    }
                    chatContainer.addView(chatMessage.getView());
                }
                chatScroller.post(()->chatScroller.fullScroll(View.FOCUS_DOWN));
            }
        }
        catch (Exception ex)
        {
            Log.e("showChat", ex.getMessage());
        }

    }


    private View messageView (ChatMessage chatMessage){
        LinearLayout box = new LinearLayout(ChatActivity.this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(15,15,15,15);
        box.setBackground(AppCompatResources.getDrawable(this, R.drawable.chat_msg));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(5,7,7,20);
        lp.gravity= Gravity.START;
        box.setLayoutParams(lp);

        TextView tv = new TextView(ChatActivity.this);
        TextView tvMsg = new TextView(ChatActivity.this);
        //прогоняем его, преобразовываем в ChatMessage и изымаем из него текст
        tv.setText(
                chatMessage.getMoment()+ " " + chatMessage.getAuthor());
        box.addView(tv);
        tvMsg.setText(chatMessage.getText());
        box.addView(tvMsg);

        //Сохраняем связь с объектом-сообщением через тег
        box.setTag(chatMessage);
        //события
        box.setOnClickListener(this::messageClick);
        return box;
    }

    private void messageClick(View view){
        ChatMessage chatMessage=(ChatMessage) view.getTag();
        Toast.makeText(this, chatMessage.getText(), Toast.LENGTH_SHORT).show();
    }

    private void loadChat(){
        try {
            //аналогично FILE объект не делает ничего, кроме создания
            //(не подключается и т.д.)
            URL url = new URL (chatUrl);
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

            runOnUiThread(()-> showChat(jsonString));

            urlStream.close();

        }
        catch (MalformedURLException ex){
            Log.d("loadChat", "MalformedURLExceprion" + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadChat", "IOException" + ex.getMessage());
        }
        catch (android.os.NetworkOnMainThreadException ex){
            Log.d("loadChat", "NetworkOnMainThreadException" + ex.getMessage());
        }
        catch (SecurityException ex){
            Log.d("loadChat", "SecurityException" + ex.getMessage());
        }
    }

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

    static class ChatMessage{
        private String id;
        private String author;
        private String text;
        private Date moment;
        //cвязь, на какой view отображено сообщение
        private View view;

        private final static SimpleDateFormat momentFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

        public ChatMessage(String author, String text) {
            this.setAuthor(author);
            this.setText(text);
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public ChatMessage(JSONObject jsonObject) throws Exception {
            this.setId(jsonObject.getString("id"));
            this.setAuthor(jsonObject.getString("author"));
            this.setText(jsonObject.getString("text"));
            this.setMoment(momentFormat.parse(jsonObject.getString("moment")));
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getMoment() {
            return moment;
        }

        public void setMoment(Date moment) {
            this.moment = moment;
        }
    }
}