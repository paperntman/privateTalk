package org.example;


import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main {


    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN")).build();
    }

}