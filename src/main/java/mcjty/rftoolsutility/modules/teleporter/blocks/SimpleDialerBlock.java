package mcjty.rftoolsutility.modules.teleporter.blocks;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.NBTTools;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SimpleDialerBlock extends LogicSlabBlock {

    public SimpleDialerBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header(),
                        parameter("transmitter", SimpleDialerBlock::getTransmitterInfo),
                        parameter("receiver", SimpleDialerBlock::getReceiverInfo),
                        parameter("once", SimpleDialerBlock::hasOnce, stack -> hasOnce(stack) ? "Once mode enabled" : ""))
                .tileEntitySupplier(SimpleDialerTileEntity::new));
    }

    private static boolean hasOnce(ItemStack stack) {
        return NBTTools.getInfoNBT(stack, CompoundNBT::getBoolean, "once", false);
    }

    private static String getTransmitterInfo(ItemStack stack) {
        if (NBTTools.hasInfoNBT(stack, "transX")) {
            int transX = NBTTools.getInfoNBT(stack, CompoundNBT::getInt, "transX", 0);
            int transY = NBTTools.getInfoNBT(stack, CompoundNBT::getInt, "transY", 0);
            int transZ = NBTTools.getInfoNBT(stack, CompoundNBT::getInt, "transZ", 0);
            String dim = NBTTools.getInfoNBT(stack, CompoundNBT::getString, "transZ", DimensionId.overworld().getRegistryName().toString());
            return transX + "," + transY + "," + transZ + " (dim " + dim + ")";
        }
        return "<unset>";
    }

    private static String getReceiverInfo(ItemStack stack) {
        return NBTTools.getInfoNBT(stack, (info, s) -> Integer.toString(info.getInt(s)), "receiver", "<unset>");
    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
        if (!world.isRemote) {
            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) world.getTileEntity(pos);
            if (simpleDialerTileEntity != null) {
                boolean onceMode = !simpleDialerTileEntity.isOnceMode();
                simpleDialerTileEntity.setOnceMode(onceMode);
                if (onceMode) {
                    Logging.message(player, "Enabled 'dial once' mode");
                } else {
                    Logging.message(player, "Disabled 'dial once' mode");
                }
            }
        }
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, p_220069_6_);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SimpleDialerTileEntity) {
            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) te;
            simpleDialerTileEntity.update();
        }
    }
}
