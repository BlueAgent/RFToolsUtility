package mcjty.rftoolsutility.modules.screen.items.modules;

import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneChannelTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.modules.RedstoneScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.RedstoneClientScreenModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneModuleItem extends GenericModuleItem {

    public RedstoneModuleItem() {
        super(new Properties()
                .maxStackSize(1)
                .defaultMaxDamage(1)
                .group(RFToolsUtility.setup.getTab()));
    }

    @Override
    protected int getUses(ItemStack stack) {
        return ScreenConfiguration.REDSTONE_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !ModuleTools.hasModuleTarget(stack);
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        return ModuleTools.getTargetString(stack);
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }


    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tag = itemStack.getTag();
        if (tag != null && tag.contains("channel")) {
            int channel = tag.getInt("channel");
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Channel: " + channel));
        }
    }

    @Override
    public Class<RedstoneScreenModule> getServerScreenModule() {
        return RedstoneScreenModule.class;
    }

    @Override
    public Class<RedstoneClientScreenModule> getClientScreenModule() {
        return RedstoneClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Red";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("Yes:").text("yestext", "Positive text").color("yescolor", "Color for the positive text").nl()
                .label("No:").text("notext", "Negative text").color("nocolor", "Color for the negative text").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").toggle("analog", "Analog mode", "Whether to show the exact level").nl()
                .label("Block:").block("monitor").nl();
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }

        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();
        TileEntity te = world.getTileEntity(pos);
        PlayerEntity player = context.getPlayer();
        Direction facing = context.getFace();
        CompoundNBT tagCompound = stack.getOrCreateTag();
        int channel = -1;
        if (te instanceof RedstoneChannelTileEntity) {
            channel = ((RedstoneChannelTileEntity) te).getChannel(true);
        } else {
            // We selected a random block.
            tagCompound.putInt("channel", -1);
            tagCompound.putString("monitordim", DimensionId.fromWorld(world).getRegistryName().toString());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            tagCompound.putInt("monitorside", facing.ordinal());
            Logging.message(player, "Redstone module is set to " + pos);

            return ActionResultType.SUCCESS;
        }

        ModuleTools.clearPositionInModule(stack);

        if (channel != -1) {
            tagCompound.putInt("channel", channel);
            Logging.message(player, "Redstone module is set to channel '" + channel + "'");
        } else {
            tagCompound.remove("channel");
            Logging.message(player, "Redstone module is cleared");
        }
        return ActionResultType.SUCCESS;
    }
}