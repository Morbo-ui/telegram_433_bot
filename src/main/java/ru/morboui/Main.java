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
import java.util.List;


public class Main {

    static class Job {
        String id;
        String name;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class HH {
        List<Job> items;

        public List<Job> getItems() {
            return items;
        }

        public void setItems(List<Job> items) {
            this.items = items;
        }

        HH() {
        }
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");


        TelegramBot bot = new TelegramBot(System.getenv("433_BOT_TOKEN"));

        bot.setUpdatesListener(element -> {
            //System.out.println(element);
            element.forEach(it -> {

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hh.ru/vacancies?text=" + it.message().text() + "&area=1"))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = response.body();
                    //System.out.println(body);
                    HH hh = mapper.readValue(response.body(), HH.class);
                    hh.items.subList(0, 5).forEach(job -> {
                        bot.execute(new SendMessage(it.message().chat().id(), "Vacancy: " + job.name + "\nLink: hh.ru/vacancy/" + job.id));
                        System.out.println(job.id + " " + job.name);
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
