package io.jwyoon1220.survival.plugin.chzzk

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.lang.IndexOutOfBoundsException


class ChzzkListener(javaPlugin: JavaPlugin) : ChatEventListener {
    private val plugin = javaPlugin
    private val NID_AUT = "YGue3cb4RiSLKfinJW0ZyZLiLErHecqaUb+D0f0pdhljhT/GJrCl4vpW9lb9TY/i"
    private val NID_SES = "AAABjKkYuzomLXC7NVDDeGdTeye100FQ/E6fug6LFTjStjlLBhqWPJeWj2VEVfgY4xQ1GvMcsf8pm8qqwrFlQXiaC8bN3Pp+LWB6Fb9LSmxzrPj64zABkOcfcxZ6HkGCm6RAkcFe+m13UHZvXqOwQGVHQOyj3XakuB3p9MYIa5Xchxrs/maWKaazEb4p+4z1usU491P0ltVrzUM2qLjx5MRoz+c7ta+YQyK3WAAcOSZmgYoVh1aCGLLcMxZLs8YqTQ1Ouojk2BmNHw0puoeL5J6gN9zOznA58kvDJeOFA+rEBAzXzcFs0eS16axaRGhb/X1J3fo8G9BotfL54wfrglbC89cWvm52/lOeihOLB97qAI2dTIEswmKKP/bMyoc0F1A9oWHxELp1JAXrJ4iFopq0+hYF/wCruclo9siX0BeaDYQeirIYjknWMUBOYogH2ue3BpogqZTHzRiGTw/9hwQjhOsVoaaEa4Imjyaqu0KxtwTZE5F2wpiQil5BDrXkwMzP93uZR8zpKt4fteX67+RUB+A="
    private lateinit var chzzkchat: ChzzkChat


    override fun onConnect(chat: ChzzkChat, isReconnecting: Boolean) {
        plugin.logger.info("재연결됨.")
        chat.connectAsync()
        chzzkchat = chat
        // !! when re-connecting, you shouldn't need to request recent chats!
        if (!isReconnecting) chat.requestRecentChat(50)
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }

    override fun onChat(msg: ChatMessage) {
        if (msg.content.contains("!등록")) {

            val message = msg.content
            val username = message.split(" ")
            Bukkit.getScheduler().runTask(plugin, Runnable {
                try {
                    val isSuccess = plugin.server.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add ${username[1]}")
                    if (isSuccess) {
                        chzzkchat.sendChat("성공! (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 등록되었습니다.")
                    } else {
                        chzzkchat.sendChat("실패. (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 화이트리스트에 등록되지 않았습니다. (정확한 닉네임이 아니거나 명령어를 제대로 작성하지 않았습니다. '!등록 <닉네임>' 으로 작성해주세요)")
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    Bukkit.broadcast(Component.text("${ex.javaClass}: "))
                }
            })
        } else {
            //for (player in Bukkit.getOnlinePlayers()) {
            //    player.sendActionBar("${msg.profile?.nickname}: ${msg.content}")
            //}
        }
    }
}