package mcjty.rftoolsutility.modules.crafter.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static mcjty.lib.builder.TooltipBuilder.*;


public class CrafterBlock extends BaseBlock implements INBTPreservingIngredient {

    public CrafterBlock(Supplier<TileEntity> tileEntitySupplier) {
        super(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .manualEntry(ManualHelper.create("rftoolsutility:machines/crafter"))
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header(), gold(),
                        parameter("contents", stack -> Integer.toString(countItems(stack))),
                        parameter("recipes", stack -> Integer.toString(countRecipes(stack))))
                .tileEntitySupplier(tileEntitySupplier));
    }

    private static int countRecipes(ItemStack itemStack) {
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound == null) {
            return 0;
        }
        ListNBT recipeTagList = tagCompound.getList("Recipes", Constants.NBT.TAG_COMPOUND);
        int rc = 0;
        for (int i = 0 ; i < recipeTagList.size() ; i++) {
            CompoundNBT tagRecipe = recipeTagList.getCompound(i);
            CompoundNBT resultCompound = tagRecipe.getCompound("Result");
            ItemStack stack = ItemStack.read(resultCompound);
            if (!stack.isEmpty()) {
                rc++;
            }
        }
        return rc;
    }

    private static int countItems(ItemStack itemStack) {
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound == null) {
            return 0;
        }
        ListNBT bufferTagList = tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);

        int rc = 0;
        for (int i = 0 ; i < bufferTagList.size() ; i++) {
            CompoundNBT itemTag = bufferTagList.getCompound(i);
            ItemStack stack = ItemStack.read(itemTag);
            if (!stack.isEmpty()) {
                rc++;
            }
        }
        return rc;
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
        return Collections.singleton("BlockEntityTag");
    }
}
