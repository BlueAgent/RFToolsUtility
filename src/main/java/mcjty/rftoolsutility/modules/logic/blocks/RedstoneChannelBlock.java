package mcjty.rftoolsutility.modules.logic.blocks;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.NBTTools;
import mcjty.rftoolsutility.modules.logic.items.RedstoneInformationItem;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.modules.screen.items.ButtonModuleItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class RedstoneChannelBlock extends LogicSlabBlock {

    public RedstoneChannelBlock(BlockBuilder builder) {
        super(builder);
    }

    protected static final String getChannelString(ItemStack stack) {
        return NBTTools.getInfoNBT(stack, (info, s) -> Integer.toString(info.getInt(s)), "channel", "<unset>");
    }

    private boolean isRedstoneChannelItem(Item item) {
        return (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof RedstoneChannelBlock) || item instanceof ButtonModuleItem
                || item instanceof RedstoneInformationItem;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        ItemStack stack = player.getHeldItem(hand);
        if (isRedstoneChannelItem(stack.getItem())) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof RedstoneChannelTileEntity) {
                if (!world.isRemote) {
                    RedstoneChannelTileEntity rcte = (RedstoneChannelTileEntity) te;
                    int channel;

                    if (stack.getItem() instanceof RedstoneInformationItem) {
                        // Add the channel (if any) to this module
                        channel = rcte.getChannel(false);
                        if (channel != -1) {
                            if (RedstoneInformationItem.addChannel(stack, channel)) {
                                Logging.message(player, TextFormatting.YELLOW + "" +
                                        "Added channel " + channel + "!");
                            } else {
                                Logging.message(player, TextFormatting.RED + "" +
                                        "Channel " + channel + " was already added!");
                            }
                        } else {
                            Logging.message(player, TextFormatting.RED + "" +
                                    "Block has no channel yet!");
                        }
                    } else {
                        if (!player.isCrouching()) {
                            // @todo 1.15: currently not working because onBlockActivated is not called when crouching
                            channel = rcte.getChannel(true);
                            NBTTools.setInfoNBT(stack, CompoundNBT::putInt, "channel", channel);
                        } else {
                            channel = NBTTools.getInfoNBT(stack, CompoundNBT::getInt, "channel", -1);
                            if (channel == -1) {
                                RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                                channel = redstoneChannels.newChannel();
                                redstoneChannels.save();
                                NBTTools.setInfoNBT(stack, CompoundNBT::putInt, "channel", channel);
                            }
                            rcte.setChannel(channel);
                        }
                        Logging.message(player, TextFormatting.YELLOW + "" +
                                "Channel set to " + channel + "!");
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, result);
    }
}
