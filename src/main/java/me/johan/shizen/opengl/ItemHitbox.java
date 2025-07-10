package me.johan.shizen.opengl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;
import static me.johan.shizen.Events.*;

public class ItemHitbox {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // list of rare items
    private static final List<Item> rareItems = Collections.unmodifiableList(Arrays.asList(Items.diamond_sword, Items.diamond_axe, Items.diamond_helmet, Items.diamond_boots, Items.iron_sword, Items.iron_chestplate, Items.iron_leggings, Items.golden_sword, Items.golden_apple));

    public static void render(){
        for (Entity entity : mc.theWorld.loadedEntityList)
        {
            // only rare item hitboxes are rendered
            if (!(entity instanceof EntityItem)) continue;
            Item item = ((EntityItem) entity).getEntityItem().getItem();
            if (!rareItems.contains(item)) continue;

            // calculate entity position relative to player
            double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * rwle.partialTicks - playerX;
            double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * rwle.partialTicks - playerY;
            double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * rwle.partialTicks - playerZ;

            // neon pink color for hitbox
            GL11.glColor4f(1f, 0.08f, 0.58f, 0.5f);

            // get the entity's bounding box
            AxisAlignedBB bb = entity.getEntityBoundingBox();

            // offset the bounding box by the entity's position
            bb = bb.offset(-entity.posX, -entity.posY, -entity.posZ);
            bb = bb.offset(entityX, entityY, entityZ);

            // draw the hitbox
            drawOutlinedBoundingBox(bb);
        }
    }

    private static void drawOutlinedBoundingBox(AxisAlignedBB bb) {
        // bottom
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);

        // top
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

        // vertical edges
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    }
}
