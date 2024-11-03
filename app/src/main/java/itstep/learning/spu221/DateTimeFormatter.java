package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeFormatter {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public static String formatMessageMoment (String moment){
        try{
            Date date = inputFormat.parse(moment);
            Calendar messageCalendar = Calendar.getInstance();
            //Была системная ошибка правильного определения текущей даты -
            // решено через настройки эмулятора (надо было установить режим Bridge)
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            //String currentDate = sdf.format(Calendar.getInstance().getTime());
            //Log.d("Current Date", currentDate);
           // Log.d("Current Date Calendar.getInstance", messageCalendar.getTime().toString());
            assert date != null:"Date should not be null";;
            messageCalendar.setTime(date);

            Calendar todayCalendar = Calendar.getInstance();
            if (isSameDay(messageCalendar,todayCalendar)){
                return "Cегодня " + timeFormat.format(date);
            }
            else if(isYesterday(messageCalendar,todayCalendar)){
                return "Вчера " + timeFormat.format(date);
            }
            else {
                return outputFormat.format(date);
            }
        }
        catch (ParseException ex)
        {
            Log.e("DateTimeFormatter", "Exception: " + ex.getMessage(), ex);
            return null;
        }
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2){
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isYesterday(Calendar msgCalendar, Calendar todayCalendar){
        Calendar yesterday = (Calendar) todayCalendar.clone();
        yesterday.add(Calendar.DAY_OF_YEAR,-1);
        return isSameDay(msgCalendar,yesterday);
    }
}
