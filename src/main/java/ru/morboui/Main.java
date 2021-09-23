package ru.morboui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Main {

  static class Bus {
    String code;
    String arrival;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public void setArrival(String arrival) {
      this.arrival = arrival;
    }

    public String getArrival() {
      return arrival;
    }
  }

  static class YA {
    List<Bus> segments;

    public List<Bus> getSegments() {
      return segments;
    }

    public void setSegments(List<Bus> segments) {
      this.segments = segments;
    }

    YA() {
    }
  }

  public static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");

    TelegramBot bot = new TelegramBot(System.getenv("433_BOT_TOKEN"));

    TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    df.setTimeZone(tz);
    String nowAsISO = df.format(new Date());

    LocalTime time = LocalTime.parse("12:33");

    bot.setUpdatesListener(element -> {
      //System.out.println(element);
      element.forEach(it -> {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(
                "https://api.rasp.yandex.net/v3.0/search/?apikey=" + System.getenv("YANDEX_API_KEY")
                    + "b7fc68cb&format=json&from=s9739796&to=s9744794&page=1&date=" + nowAsISO
                    + "&levent=departure"))
            .build();

        try {
          HttpResponse<String> response = client
              .send(request, HttpResponse.BodyHandlers.ofString());
          ObjectMapper mapper = new ObjectMapper();
          mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          String body = response.body();
          //System.out.println(body);
          YA ya = mapper.readValue(response.body(), YA.class);
          ya.segments.forEach(bus -> {
            //bot.execute(new SendMessage(it.message().chat().id(), "Vacancy: "));
            String s = bus.arrival;
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(s);
            Instant i = Instant.from(ta);
            Date d = Date.from(i);

            System.out.println(d + " " + bus.code);
          });
          response.body();
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
        }

      });

      return UpdatesListener.CONFIRMED_UPDATES_ALL;
    });

  }
}
