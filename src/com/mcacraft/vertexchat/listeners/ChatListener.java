package com.mcacraft.vertexchat.listeners;

import com.mcacraft.vertexchat.VertexChat;
import com.mcacraft.vertexchat.VertexChatAPI;
import com.mcacraft.vertexchat.chat.ChatManager;
import com.mcacraft.vertexchat.util.VConfig;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author Kenny
 */
public class ChatListener implements Listener
{
    private VertexChat plugin;
    public ChatListener(VertexChat instance)
    {
        this.plugin = instance;
    }
    
    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String msgOriginal = event.getMessage();
        //Using different method for fixing the stupid /reload issue
//        if(!ChatManager.hasChannel(player.getName()))
//        {
//            VConfig data = new VConfig(plugin.getDataFolder()+File.separator+"data", "focused-channels.yml", plugin);
//            for(String s : data.getConfig().getStringList(player.getName()))
//            {
//                ChatManager.getChannel(s).addPlayer(player.getName());
//                ChatManager.addChannelToPlayer(player.getName(), s);
//            }
//            ChatManager.setFocusedChannel(player.getName(), ChatManager.getDefaultChannel());
//            ChatManager.setSilentChat(player.getName(), Boolean.FALSE);
//        }
        
        //If the player is muted cancel the chat
        if(ChatManager.isMuted(player.getName()))
        {
            player.sendMessage(ChatColor.RED+"You are currently muted.");
            event.setCancelled(true);
            return;
        }
        // If the player has their chat silenced
        if(ChatManager.isChatSilenced(player.getName()))
        {
            player.sendMessage(ChatColor.RED+"Your chat is currently silenced. Type "+ChatColor.YELLOW+"/silence"+ChatColor.RED+" to re-enable the chat.");
            event.setCancelled(true);
            return;
        }
        //If the channel the player is talking in does not exist. If a channel got deleted and somehow they are still in it
        if(!ChatManager.channelExists(ChatManager.getFocusedChannelName(player.getName())))
        {
            player.sendMessage(ChatColor.RED+"You are not in a valid channel. Type "+ChatColor.YELLOW+"/ch list"+ChatColor.RED+" for a list of channels.");
            event.setCancelled(true);
            return;
        }
        //Sends the chat message to all players in the channel
        for(String s : ChatManager.getFocusedChannel(player.getName()).getPlayers())
        {
            //Check to make sure it is sending to only players with their chat activated
            if(!ChatManager.isChatSilenced(s))
            {
                Player p = Bukkit.getPlayer(s);
                p.sendMessage(VertexChatAPI.parseFormat(msgOriginal, player, ChatManager.getFocusedChannelName(player.getName())));
            }
        }
        
        //If this channel should be logged to the console
        VConfig chConfig = new VConfig(plugin.getDataFolder()+File.separator+"channels", ChatManager.getFocusedChannelName(player.getName())+".yml", plugin);
        if(chConfig.getConfig().getBoolean("verbose"))
        {
            Bukkit.getLogger().log(Level.INFO, VertexChatAPI.logParseFormat(msgOriginal, player, ChatManager.getFocusedChannelName(player.getName())));
        }
        
        //Logs the chat to a seperate log file. 
        if(plugin.getConfig().getBoolean("log-chat"))
        {
            //For increase in speed the logging should be done in intervals. Do this later
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            VertexChatAPI.logToFile(dateFormat.format(date)+" - "+VertexChatAPI.logParseFormat(msgOriginal, player, ChatManager.getFocusedChannelName(player.getName())));
        }
        event.setCancelled(true);
    }

}
