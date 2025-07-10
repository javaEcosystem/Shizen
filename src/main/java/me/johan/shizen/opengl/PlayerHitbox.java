package me.johan.shizen.opengl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;
import static me.johan.shizen.Events.*;

public class PlayerHitbox {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void render(){
        for (Entity entity : mc.theWorld.loadedEntityList)
        {
            // only players hitboxes are rendered
            if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;

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
