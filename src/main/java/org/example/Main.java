package org.example;


import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {

    static Long registrationChannel = null;
    static Long registrationUser = null;
    static List<Long> registrationTarget = new ArrayList<>();
    static List<Message> registrationStart = new ArrayList<>();
    static final Long civilianId = 921956686346985543L;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN")).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new Main()).build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getMessage().getContentRaw().equalsIgnoreCase("bot setup")){
            System.out.println("Bot setup started");
            event.getMessage().delete().queue();
            for (Command command : event.getGuild().retrieveCommands().complete()) {
                command.delete().queue();
            }
            event.getGuild().updateCommands().addCommands(
                    Commands.slash("등록", "개인 공간 등록을 시작합니다."),
                    Commands.slash("완료", "개인 공간 등록을 완료합니다.")
            ).queue();
            return;
        }
        if(registrationChannel == null) return;
        if (
                registrationUser == event.getMember().getIdLong()
                &&
                event.getChannel().getIdLong() == registrationChannel)
        {
            registrationTarget.addAll(event.getMessage().getMentions().getMentions(Message.MentionType.USER).stream().map(ISnowflake::getIdLong).toList());
            if(!event.getMessage().getMentions().getMentions(Message.MentionType.USER).isEmpty()) registrationStart.add(event.getMessage());
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getName().equalsIgnoreCase("등록")) {
            registrate(event);
        }else if (event.getName().equalsIgnoreCase("완료")){
            finish(event);
        }
    }

    private static void finish(SlashCommandInteractionEvent event) {
        if (registrationChannel == null){
            event.getHook().sendMessage("등록을 진행해 주세요! /등록").queue();
            return;
        }
        if (registrationChannel != event.getChannelIdLong()){
            event.getHook().sendMessage("다른 채널에서 등록이 진행 중입니다!").queue();
            return;
        }
        VoiceChannel aPrivate = event.getGuild().createVoiceChannel("private").complete();
        VoiceChannelManager manager = aPrivate.getManager();
        manager = manager.putRolePermissionOverride(
                event.getGuild().getPublicRole().getIdLong(),
                0,
                Permission.getRaw(Permission.VIEW_CHANNEL)
        );
        manager = manager.putRolePermissionOverride(
                civilianId,
                0,
                Permission.getRaw(Permission.VIEW_CHANNEL)
        );
        for (Long l : registrationTarget) {
            manager = manager.putMemberPermissionOverride(
                    l,
                    EnumSet.of(Permission.VIEW_CHANNEL),
                    EnumSet.noneOf(Permission.class)
            );
        }
        manager.queue();
        event.getHook().sendMessage("등록이 완료되었습니다! "+aPrivate.getAsMention()).complete().delete().queueAfter(3, TimeUnit.SECONDS);
        registrationStart.forEach(i -> i.delete().queueAfter(3, TimeUnit.SECONDS, _ -> {}, _ -> System.out.println("failed!")));
        registrationStart.clear();
        registrationTarget.clear();
        registrationUser = null;
        registrationChannel = null;
    }

    private static void registrate(SlashCommandInteractionEvent event) {
        if(registrationChannel != null) {
            event.getHook().sendMessage("이미 등록이 진행 중입니다!").queue();
            return;
        }
        registrationStart.add(event.getHook().sendMessage("등록을 시작합니다!").complete());
        registrationChannel = event.getChannelIdLong();
        registrationUser = event.getUser().getIdLong();
        registrationTarget.add(registrationUser);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() == null) return;
        VoiceChannel voiceChannel = event.getChannelLeft().asVoiceChannel();
        if (voiceChannel.getName().equals("private")) {
            if (voiceChannel.getMembers().isEmpty()) {
                voiceChannel.delete().queue();
            }
        }
    }
}