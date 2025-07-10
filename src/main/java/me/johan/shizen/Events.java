package me.johan.shizen;

import me.johan.shizen.auth.Account;
import me.johan.shizen.auth.SessionManager;
import me.johan.shizen.gui.GuiAccountManager;
import me.johan.shizen.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public class Events {
  private static final Minecraft mc = Minecraft.getMinecraft();

  @SubscribeEvent
  public void onRenderTick(TickEvent.RenderTickEvent event) {
    if (event.phase != TickEvent.Phase.END || mc.currentScreen == null) {
      return;
    }

    if (mc.currentScreen instanceof GuiSelectWorld || mc.currentScreen instanceof GuiMultiplayer) {
      String text = TextFormatting.translate(String.format(
        "&7Username: &3%s&r", SessionManager.get().getUsername()
      ));
      GlStateManager.disableLighting();
      mc.currentScreen.drawString(mc.fontRendererObj, text, 3, 3, -1);
      GlStateManager.enableLighting();
    }
  }

  @SubscribeEvent
  public void initGuiEvent(InitGuiEvent.Post event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      event.buttonList.add(new GuiButton(
        69, event.gui.width - 106, 6, 100, 20, "Accounts"
      ));
    }

    if (event.gui instanceof GuiDisconnected) {
      try {
        Field f = ReflectionHelper.findField(GuiDisconnected.class, "message", "field_146304_f");
        IChatComponent message = (IChatComponent) f.get(event.gui);
        String text = message.getFormattedText().split("\n\n")[0];
        if (
          text.equals("§r§cYou are permanently banned from this server!") ||
          text.equals("§r§cYour account has been blocked.")
        ) {
          Shizen.load();
          for (Account account : Shizen.accounts) {
            if (mc.getSession().getUsername().equals(account.getUsername())) {
              account.setUnban(-1L);
            }
          }
          Shizen.save();
          return;
        }

        if (
          text.matches("§r§cYou are temporarily banned for §r§f.*§r§c from this server!") ||
          text.matches("§r§cYour account is temporarily blocked for §r§f.*§r§c from this server!")
        ) {
          String unban = StringUtils.substringBetween(text, "§r§f", "§r§c");
          if (unban != null) {
            long time = System.currentTimeMillis();
            for (String duration : unban.split(" ")) {
              String type = duration.substring(duration.length() - 1);
              long value = Long.parseLong(duration.substring(0, duration.length() - 1));
              switch (type) {
                case "d": {
                  time += value * 86400000L;
                }
                break;
                case "h": {
                  time += value * 3600000L;
                }
                break;
                case "m": {
                  time += value * 60000L;
                }
                break;
                case "s": {
                  time += value * 1000L;
                }
                break;
              }
            }

            Shizen.load();
            for (Account account : Shizen.accounts) {
              if (mc.getSession().getUsername().equals(account.getUsername())) {
                account.setUnban(time);
              }
            }
            Shizen.save();
          }
        }
      } catch (Exception e) {
        //
      }
    }
  }

  @SubscribeEvent
  public void onClick(ActionPerformedEvent event) {
    if (event.gui instanceof GuiSelectWorld || event.gui instanceof GuiMultiplayer) {
      if (event.button.id == 69) {
        mc.displayGuiScreen(new GuiAccountManager(event.gui));
      }
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    ServerData serverData = mc.getCurrentServerData();
    if (serverData != null) {
      String serverIP = serverData.serverIP;
      if (serverIP.endsWith("hypixel.net") || serverIP.endsWith("hypixel.io")) {
        Shizen.load();
        for (Account account : Shizen.accounts) {
          if (mc.getSession().getUsername().equals(account.getUsername())) {
            account.setUnban(0L);
          }
        }
        Shizen.save();
      }
    }
  }
}
