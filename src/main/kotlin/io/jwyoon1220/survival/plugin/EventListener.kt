package io.jwyoon1220.survival.plugin

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import kotlin.random.Random


class EventListener(plugins: JavaPlugin, zombies: Team, players: Team) : Listener {

    private val plugin: JavaPlugin = plugins
    private val config = ConfigManager(plugin)
    private val playerT: Team = players
    private val zombieT: Team = zombies

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        /*
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team add player \"생존자\"")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team add zombie \"좀비\"")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify player color green")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify zombie color red")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify player nametagVisibility hideForOtherTeams")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team modify zombie nametagVisibility hideForOtherTeams")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team leave ${event.player.name}")
        */
        event.joinMessage(null)
        if (config.getValue("partner.${event.player.name}") == true) {
            if (!playerT.hasEntry(event.player.name)) {
                playerT.addEntry(event.player.name)
            }
            event.player.maxHealth = 20.0
            if (config.getValue("${event.player.name}.isJoined") != true) {
                event.player.sendMessage("처음 접속하셨군요! 당신은 생존자 입니다.\n좀비의 추격을 피하고, 다른 생존자들과 함께 공동체를 이루어 살아가세요!\n행운을 빕니다.")
            }

        } else {
            if (!zombieT.hasEntry(event.player.name)) {
                zombieT.addEntry(event.player.name)
            }
            if (config.getValue("${event.player.name}.isJoined") != true) {
                event.player.sendMessage("처음 접속하셨군요! 당신은 좀비 입니다.\n생존자를 찾아 좀비로 만드세요!")
            }
            event.player.maxHealth = 10.0
        }
        config.setValue("${event.player.name}.isJoined", true)

    }
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(null)
    }
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        //Bukkit.dispatchCommand(event.player as CommandSender, "teammsg ${event.message}")
        if (event.player.isZombie()) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.isZombie()) {
                    player.sendMessage("${ChatColor.RED}[좀비 채팅] ${ChatColor.RESET}${event.player.name}: ${event.message}")
                    plugin.logger.info("${ChatColor.RED}[좀비 채팅] ${ChatColor.RESET}${event.player.name}: ${event.message}")
                }
            }
        } else if (event.player.isPlayer()) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.isPlayer()) {
                    player.sendMessage("${ChatColor.GREEN}[생존자 채팅] ${ChatColor.RESET}${event.player.name}: ${event.message}")
                    plugin.logger.info("${ChatColor.GREEN}[생존자 채팅] ${ChatColor.RESET}${event.player.name}: ${event.message}")
                }
            }
        }
    }
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        event.player.gameMode = GameMode.SPECTATOR
        val loc = event.player.bedSpawnLocation ?: event.player.world.spawnLocation
        if (event.player.isPlayer()) {
            event.deathMessage(Component.text("${ChatColor.RED}${event.player.name}이(가) 죽어 좀비가 되었습니다."))
            playerDie(loc, event.player)
        }
        if (event.player.isZombie()) {
            event.deathMessage(Component.text("${event.player.name}이(가) 죽었습니다."))
            event.player.sendActionBar("10초 후에 리스폰 됩니다.")
            event.player.maxHealth = 10.0

            val random = Random(System.nanoTime())
            val chance = random.nextInt(100)
            if (chance < 40) {
                event.drops.add(ItemStack(Material.ZOMBIE_HEAD))
            }
            object : BukkitRunnable() {
                override fun run() {
                    // 이 블록 내에서 10초 대기
                    // 10초 후 실행할 코드
                    event.player.sendActionBar("리스폰")
                    event.player.teleportAsync(loc)
                    event.player.gameMode = GameMode.SURVIVAL
                }
            }.runTaskLater(plugin, (10L*20L))
        }
    }

   @EventHandler
   fun onInteract(event: PlayerInteractEvent) {

       val vaccineItem = ItemStack(Material.GHAST_TEAR)

       // 아이템 메타 설정
       val meta: ItemMeta = vaccineItem.itemMeta ?: return
       meta.setDisplayName("${ChatColor.GREEN}좀비 바이러스 백신(고(固)형제)") // 아이템 이름
       meta.lore = listOf("백신이지만 치료제도 된다고 한다.", "뭔가 맛이 매우 없을거 같다.") // 아이템 설명
       vaccineItem.itemMeta = meta

       val action = event.action
       if (config.getValue("partner.${event.player.name}") != true) {
           if (action == Action.RIGHT_CLICK_AIR) {
               if (event.player.inventory.itemInMainHand == vaccineItem) {
                   Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "attach partner ${event.player.name}")
                   playerT.addEntry(event.player.name)
                   Bukkit.broadcast(Component.text("${ChatColor.GREEN}${event.player.name}이(가) 좀비 바이러스로부터 해방되었습니다."))
                   val meta = event.player.inventory.itemInMainHand.itemMeta
                   meta.setDisplayName("${ChatColor.GRAY}좀비 바이러스 백신(사용됨)")
                   event.player.inventory.itemInMainHand.itemMeta = meta
                   event.player.playSound(event.player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F)
                   event.player.maxHealth = 20.0
               }
           }
           if (action == Action.RIGHT_CLICK_BLOCK) {
               if (event.player.inventory.itemInMainHand == vaccineItem) {
                   Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "attach partner ${event.player.name}")
                   playerT.addEntry(event.player.name)
                   Bukkit.broadcast(Component.text("${ChatColor.GREEN}${event.player.name}이(가) 좀비 바이러스로부터 해방되었습니다."))
                   val meta = event.player.inventory.itemInMainHand.itemMeta
                   meta.setDisplayName("${ChatColor.GRAY}좀비 바이러스 백신(사용됨)")
                   event.player.inventory.itemInMainHand.itemMeta = meta
                   event.player.playSound(event.player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F)
                   event.player.maxHealth = 20.0
               }
           }
       }
   }
    @EventHandler
    fun onZombieSpawn(event: CreatureSpawnEvent) {
        if (event.entity is Zombie) {
            val zombieEntity = event.entity
            zombieEntity.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000, 3))
            zombieEntity.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1000000, 2))
            zombieEntity.equipment?.helmet = ItemStack(Material.DIAMOND_HELMET)
            zombieEntity.equipment?.setItemInMainHand(ItemStack(Material.IRON_SWORD))

        }
        if (event.entity is Skeleton) {
        event.isCancelled = true
        }
    }

    @EventHandler
    fun onTarget(event: EntityTargetEvent) {
        if (event.entity is Zombie && event.target is Player) {
            val zombie = event.entity as Zombie
            val player = event.target as Player

            val team = player.scoreboard.getEntryTeam(player.name)
            if (team != null && team.name == "zombie") {
                event.isCancelled = true

            }
        }
    }
    @EventHandler
    fun onCraft(event: CraftItemEvent) {
        if (event.inventory.result?.type == Material.BOW) {
            event.isCancelled = true
            event.whoClicked.sendMessage("활은 너무 밸붕이여서 제작법 바꿈.")
        }
    }
    @EventHandler
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        if (event.entity is Arrow) {
            val arrow = event.entity as Arrow
            if (arrow.isCritical) {
                arrow.velocity = arrow.velocity.multiply(4)
                arrow.isGlowing = true
                arrow.damage = 10.0
                arrow.pierceLevel = 4
                arrow.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:entity.generic.explode"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 2f))
                val loc = event.entity.location
                val players = loc.getNearbyPlayers(10.0)
                for (player in players) {
                    player.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:entity.generic.explode"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 2f))
                }
            } else {
                arrow.velocity = arrow.velocity.multiply(2)
                arrow.damage = 5.0
                arrow.pierceLevel = 3
            }
        }
    }


    // functions
    private fun playerDie(loc: Location, player: Player) {
        playerT.removeEntry(player.name)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "disattach partner ${player.name}")
        zombieT.addEntry(player.name)
        player.sendActionBar("10초 후에 리스폰 됩니다.")
        player.maxHealth = 10.0
        object : BukkitRunnable() {
            override fun run() {
                // 이 블록 내에서 10초 대기
                // 10초 후 실행할 코드
                player.sendActionBar("리스폰")
                player.teleportAsync(loc)
                player.gameMode = GameMode.SURVIVAL
                player.sendMessage("당신은 좀비 입니다.\n생존자를 찾아 좀비로 만드세요!")
            }
        }.runTaskLater(plugin, (10L*20L)) // 메인 스레드에서 실행
    }
    fun Player.isZombie(): Boolean {
        val plugin = server.pluginManager.getPlugin("Survival")
        val config = plugin?.config
        if (!config!!.getBoolean("partner.${player?.name}")) {
            return true
        } else {
            return false
        }
    }
    fun Player.isPlayer() : Boolean {
        val plugin = server.pluginManager.getPlugin("Survival")
        val config = plugin?.config
        if (config!!.getBoolean("partner.${player?.name}")) {
            return true
        } else {
            return false
        }
    }
    fun PlayerJoinEvent.isFirstJoin() : Boolean {
        return plugin.config.getBoolean("${player.name}.isJoined")
    }
}
