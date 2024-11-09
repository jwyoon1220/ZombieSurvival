package io.jwyoon1220.survival.plugin

import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.scoreboard.Team
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat

class RegisterKommand(javaPlugin: JavaPlugin, chat: ChzzkChat) {
    private val config: ConfigManager = ConfigManager(javaPlugin)
    init {
        javaPlugin.kommand {

            register("attach") {
                requires { isOp }
                then("partner") {
                    then("player" to string()) {
                        executes {
                            val player: String by it
                            config.setValue("partner.$player", true)
                            sender.sendMessage("partner.$player = true")
                            if (Bukkit.getPlayer(player) != null) {
                                val manager: ScoreboardManager = Bukkit.getScoreboardManager()
                                val playerTeam: Team? = manager.newScoreboard.getTeam("player")
                                playerTeam?.addEntry(player)
                            }
                        }
                    }
                }
            }
            register("disattach") {
                requires { isOp }
                then("partner") {
                    then("player" to string()) {
                        executes {
                            val player: String by it
                            config.setValue("partner.$player", false)
                            sender.sendMessage("partner.$player = false")
                            if (Bukkit.getPlayer(player) != null) {
                                val manager: ScoreboardManager = Bukkit.getScoreboardManager()
                                val playerTeam: Team? = manager.newScoreboard.getTeam("zombie")
                                playerTeam?.addEntry(player)
                            }
                        }
                    }
                }
            }
            register("setconfig") {
                requires { isOp }
                then("key" to string()) {
                    then("value" to string()) {
                        executes {
                            val key: String by it
                            val value: String by it
                            config.setValue(key, value)
                            sender.sendMessage("$key = ${config.getValue(key)}")
                        }
                    }
                }
            }
            register("vaccine") {
                requires { isOp && isPlayer }
                executes {
                    val vaccineItem = ItemStack(Material.GHAST_TEAR)

                    // 아이템 메타 설정
                    val meta: ItemMeta = vaccineItem.itemMeta
                    meta.setDisplayName("${ChatColor.GREEN}좀비 바이러스 백신(고(固)형제)") // 아이템 이름
                    meta.lore = listOf("백신이지만 치료제도 된다고 한다.", "뭔가 맛이 매우 없을거 같다.") // 아이템 설명
                    vaccineItem.itemMeta = meta
                    player.inventory.addItem(vaccineItem)
                }
            }
            register("yangmung-pass") {
                requires { isOp }
                executes {
                    player.performCommand("whitelist add ysdydys")
                    player.performCommand("attach partner ysdydys")
                    Bukkit.broadcast(Component.text("양뭉-패스가 등록되었습니다. 이제 양뭉은 서버에 들어올수 있습니다."))

                }
            }
            // 메시지 전송 기능, 하지만 NID 토큰 필요.
            register("send") {
                requires { isOp }
                then("msg" to string(StringType.GREEDY_PHRASE)) {
                    executes {
                        val msg: String by it
                        chat.sendChat(msg)
                    }
                }
            }

        }
    }
}