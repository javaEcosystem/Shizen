package me.johan.shizen;

import me.johan.shizen.auth.Account;
import me.johan.shizen.auth.SessionManager;
import me.johan.shizen.gui.GuiAccountManager;
import me.johan.shizen.opengl.ItemHitbox;
import me.johan.shizen.opengl.PlayerHitbox;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Field;
import org.lwjgl.opengl.GL11;

import static me.johan.shizen.Shizen.itemHitboxKey;
import static me.johan.shizen.Shizen.playerHitboxKey;

public class Events {
  private static final Minecraft mc = Minecraft.getMinecraft();
  public static RenderWorldLastEvent rwle;

  // player-related variables
  public static double playerX;
  public static double playerY;
  public static double playerZ;

  // boolean var. to switch hitbox visibility
  public static boolean showPlayerHitbox = false;
  public static boolean showItemHitbox = false;

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
          Shizen.loadAccounts();
          for (Account account : Shizen.accounts) {
            if (mc.getSession().getUsername().equals(account.getUsername())) {
              account.setUnban(-1L);
            }
          }
          Shizen.saveAccounts();
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

            Shizen.loadAccounts();
            for (Account account : Shizen.accounts) {
              if (mc.getSession().getUsername().equals(account.getUsername())) {
                account.setUnban(time);
              }
            }
            Shizen.saveAccounts();
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
        Shizen.loadAccounts();
        for (Account account : Shizen.accounts) {
          if (mc.getSession().getUsername().equals(account.getUsername())) {
            account.setUnban(0L);
          }
        }
        Shizen.saveAccounts();
      }
    }
  }

  @SubscribeEvent
  public void onKeyInput(InputEvent.KeyInputEvent event) {
    // toggle displays
    if (playerHitboxKey.isPressed()) { showPlayerHitbox = !showPlayerHitbox; }
    if (itemHitboxKey.isPressed()) { showItemHitbox = !showItemHitbox; }
  }

  @SubscribeEvent
  public void onRenderWorld(RenderWorldLastEvent event) {
    // leave early if all rendering features are disabled
    if ((!showItemHitbox && !showPlayerHitbox) || mc.theWorld == null || mc.thePlayer == null) return;

    // Apply event to the global variable so that we can use it everywhere
    rwle = event;

    // get player position (interpolated for smooth rendering)
    playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * rwle.partialTicks;
    playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * rwle.partialTicks;
    playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * rwle.partialTicks;

    // prepare OpenGL state for 3D line rendering
    GlStateManager.disableTexture2D();
    GlStateManager.disableDepth();
    GlStateManager.depthMask(false);

    GL11.glEnable(GL11.GL_LINE_SMOOTH);
    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    GL11.glLineWidth(0.1F);
    GL11.glBegin(GL11.GL_LINES);

    if(showPlayerHitbox){
      PlayerHitbox.render();
    }

    if(showItemHitbox){
      ItemHitbox.render();
    }

    // restore default OpenGL state
    GL11.glEnd();
    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    GL11.glColor4f(1f, 1f, 1f, 1f);

    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableTexture2D();

    }
  }
