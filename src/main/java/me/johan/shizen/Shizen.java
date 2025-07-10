package me.johan.shizen;

import com.google.gson.*;
import me.johan.shizen.auth.Account;
import me.johan.shizen.utils.SSLUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

@Mod(modid = "shizen", version = "@VERSION@", clientSideOnly = true, acceptedMinecraftVersions = "1.8.9")
public class Shizen {

  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final File file = new File(mc.mcDataDir, "accounts.json");
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  public static final ArrayList<Account> accounts = new ArrayList<>();

  @EventHandler
  public static void init(FMLInitializationEvent event) {
    SSLContext ignored = SSLUtils.getSSLContext();
    MinecraftForge.EVENT_BUS.register(new Events());

    if (!file.exists()) {
      try {
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
          if (file.createNewFile()) {
            System.out.print("Successfully created accounts.json!");
          }
        }
      } catch (IOException e) {
        System.err.print("Couldn't create accounts.json!");
      }
    }
  }

  public static void load() {
    accounts.clear();
    try {
      JsonElement json = new JsonParser().parse(
        new BufferedReader(new FileReader(file))
      );
      if (json instanceof JsonArray) {
        JsonArray jsonArray = json.getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          accounts.add(new Account(
            Optional.ofNullable(jsonObject.get("refreshToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("accessToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("username")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(jsonObject.get("unban")).map(JsonElement::getAsLong).orElse(0L)
          ));
        }
      }
    } catch (FileNotFoundException e) {
      System.err.print("Couldn't find accounts.json!");
    }
  }

  public static void save() {
    try {
      JsonArray jsonArray = new JsonArray();
      for (Account account : accounts) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("refreshToken", account.getRefreshToken());
        jsonObject.addProperty("accessToken", account.getAccessToken());
        jsonObject.addProperty("username", account.getUsername());
        jsonObject.addProperty("unban", account.getUnban());
        jsonArray.add(jsonObject);
      }
      PrintWriter printWriter = new PrintWriter(new FileWriter(file));
      printWriter.println(gson.toJson(jsonArray));
      printWriter.close();
    } catch (IOException e) {
      System.err.print("Couldn't save accounts.json!");
    }
  }
}
