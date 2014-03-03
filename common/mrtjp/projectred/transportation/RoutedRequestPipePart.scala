package mrtjp.projectred.transportation

import codechicken.lib.packet.PacketCustom
import mrtjp.projectred.core.utils.ItemKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.ForgeDirection
import net.minecraft.entity.item.EntityItem

class RoutedRequestPipePart extends RoutedJunctionPipePart
{
    override def centerReached(r:RoutedPayload)
    {
        if (!maskConnects(r.output.ordinal) && !world.isRemote) if (itemFlow.scheduleRemoval(r))
        {
            r.resetTrip
            r.moveProgress(0.375F)
            r.speed = 0.075F
            val ent = r.getEntityForDrop(x, y, z)
            ent.posY += 0.1F
            world.spawnEntityInWorld(ent)
        } else println("CANNOT REMOVE FROM FLOW")
    }

    override def activate(player:EntityPlayer, hit:MovingObjectPosition, item:ItemStack):Boolean =
    {
        if (super.activate(player, hit, item)) return true
        if (!world.isRemote) openGui(player)
        true
    }

    private def openGui(player:EntityPlayer)
    {
        val packet = new PacketCustom(TransportationSPH.channel, TransportationSPH.gui_Request_open)
        packet.writeCoord(x, y, z)
        packet.sendToPlayer(player)
    }

    override def getDirForIncomingItem(r:RoutedPayload):ForgeDirection =
    {
        val dir = ForgeDirection.getOrientation(inOutSide)
        if (dir == ForgeDirection.UNKNOWN)
        {
            val count =
            {
                var c = 0
                for (i <- 0 until 6) if ((connMap&1<<i) != 0) c += 1
                c
            }

            if (count <= 1) return r.input
            else if (count == 2)
            {
                for (i <- 0 until 6) if (i != r.input.getOpposite.ordinal)
                    if ((connMap&1<<i) != 0) return ForgeDirection.getOrientation(i)
            }
        }
        dir
    }

    override def getActiveFreeSpace(item:ItemKey) =
    {
        if (getInventory != null) super.getActiveFreeSpace(item)
        else Integer.MAX_VALUE
    }

    override def getType = "pr_rrequest"
}