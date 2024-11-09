package io.jwyoon1220.survival.plugin.chzzk

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.ChzzkBuilder
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.types.channel.ChzzkChannel


class ChzzkListener(javaPlugin: JavaPlugin) : ChatEventListener {
    private val plugin: JavaPlugin = javaPlugin
    private lateinit var chzzkChat: ChzzkChat

    override fun onConnect(chat: ChzzkChat, isReconnecting: Boolean) {
        plugin.logger.info("재연결됨.")
        chat.connectAsync()
        chzzkChat = chat
        // !! when re-connecting, you shouldn't need to request recent chats!
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }

    override fun onChat(msg: ChatMessage) {
       if (msg.profile?.nickname != "지윤1220") {
           if (msg.content.contains("!등록")) {

               val message = msg.content
               val username = message.split(" ")
               Bukkit.getScheduler().runTask(plugin, Runnable {
                   try {
                       val isSuccess = plugin.server.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add ${username[1]}")
                       if (isSuccess) {
                           chzzkChat.sendChat("성공! (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 등록되었습니다.")
                       } else {
                           chzzkChat.sendChat("실패. (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 화이트리스트에 등록되지 않았습니다. (정확한 닉네임이 아니거나 명령어를 제대로 작성하지 않았습니다. '!등록 <닉네임>' 으로 작성해주세요)")
                       }
                   } catch (ex: IndexOutOfBoundsException) {
                       chzzkChat.sendChat("명령어 형식이 올바르지 않습니다. '!등록 <자신의 마인크래프트 닉네임> 형식으로 명령어를 다시 입력하여 주세요.")
                   }
               })
           } else if (msg.content.contains("!시참")) {
               chzzkChat.sendChat("서버 주소: yangmung1.mcv.kr")
               chzzkChat.sendChat("버전: 1.17.1")
           } else if (msg.content.contains("!ping")) {
               chzzkChat.sendChat("자고로 봇이란 !ping을 할수 있어야 한다.")
               chzzkChat.sendChat("pong!")
           } else if (msg.content == "?" || msg.content == "??" || msg.content == "???" || msg.content == "????" || msg.content == "?????" || msg.content == "??????" ) {
               chzzkChat.sendChat("??? 뭐요")
           }
       }
    }
}