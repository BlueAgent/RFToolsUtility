package mcjty.rftoolsutility.modules.crafter.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.crafter.CrafterSetup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends BaseBlock implements INBTPreservingIngredient
        /*, IRedstoneConnectable*/ {

    public CrafterBlock(Supplier<TileEntity> tileEntitySupplier) {
        super(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .tileEntitySupplier(tileEntitySupplier));
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            ListNBT bufferTagList = tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);
            ListNBT recipeTagList = tagCompound.getList("Recipes", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.size() ; i++) {
                CompoundNBT itemTag = bufferTagList.getCompound(i);
                if (itemTag != null) {
                    ItemStack stack = ItemStack.read(itemTag);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(new StringTextComponent(TextFormatting.GREEN + "Contents: " + rc + " stacks"));

            rc = 0;
            for (int i = 0 ; i < recipeTagList.size() ; i++) {
                CompoundNBT tagRecipe = recipeTagList.getCompound(i);
                CompoundNBT resultCompound = tagRecipe.getCompound("Result");
                if (resultCompound != null) {
                    ItemStack stack = ItemStack.read(resultCompound);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(new StringTextComponent(TextFormatting.GREEN + "Recipes: " + rc + " recipes"));
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            int amount = 2;
            if (itemStack.getItem() == CrafterSetup.CRAFTER1_ITEM.get()) {
                amount = 2;
            } else if (itemStack.getItem() == CrafterSetup.CRAFTER2_ITEM.get()) {
                amount = 4;
            } else {
                amount = 8;
            }
            list.add(new StringTextComponent(TextFormatting.WHITE + "This machine can handle up to " + amount + " recipes"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "at once and allows recipes to use the crafting results"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "of previous steps."));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + RFToolsUtility.SHIFT_MESSAGE));
        }
    }

    // @todo 1.14
//    @Override
//    protected IModuleSupport getModuleSupport() {
//        return new ModuleSupport(CrafterContainer.SLOT_FILTER_MODULE) {
//            @Override
//            public boolean isModule(ItemStack itemStack) {
//                return itemStack.getItem() == ModularStorageSetup.storageFilterItem;
//            }
//        };
//    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, Direction from) {
//        return true;
//    }
//

    @Override
    public Collection<String> getTagsToPreserve() {
        return Collections.singleton("Info");
    }
}
