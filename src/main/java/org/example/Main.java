package org.example;


import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class Main extends ListenerAdapter {

    static String registrationChannel = null;
    static String registrationUser = null;
    static List<String> registrationTarget = new ArrayList<>();

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN")).build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getMessage().getContentRaw().equalsIgnoreCase("bot setup")){
            event.getMessage().delete().queue();
            for (Command command : event.getGuild().retrieveCommands().complete()) {
                command.delete().queue();
            }
            event.getGuild().updateCommands().addCommands(
                    Commands.slash("등록", "개인 공간 등록을 시작합니다."),
                    Commands.slash("완료", "개인 공간 등록을 완료합니다.")
            ).queue();
        }
        if (registrationUser.equals(event.getMember().getId())
                && event.getChannel().getId().equals(registrationChannel)){
            registrationTarget.addAll(event.getMessage().getMentions().getMentions(Message.MentionType.USER).stream().map(ISnowflake::getId).toList());
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("등록")) {
            registrate(event);
        }else if (event.getName().equalsIgnoreCase("완료")){
            if (registrationChannel == null){
                event.reply("등록을 진행해 주세요! /등록").queue();
                return;
            }
            if (!registrationChannel.equals(event.getChannelId())){
                event.reply("다른 채널에서 등록이 진행 중입니다!").queue();
                return;
            }
            
        }
    }

    private static void registrate(SlashCommandInteractionEvent event) {
        if(registrationChannel == null) {
            event.reply("이미 등록이 진행 중입니다!").queue();
            return;
        }
        event.reply("등록을 시작합니다!").queue();
        registrationChannel = event.getChannelId();
        registrationUser = event.getUser().getId();
    }
}