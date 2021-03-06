package mcjty.rftoolsutility.modules.logic.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.*;

;

public class RedstoneReceiverTileEntity extends RedstoneChannelTileEntity implements ITickableTileEntity {

    public static final String CMD_SETANALOG = "receiver.setAnalog";

    private boolean analog = false;

    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Redstone Receiver")
            .containerSupplier((windowId,player) -> new GenericContainer(LogicBlockModule.CONTAINER_REDSTONE_RECEIVER.get(), windowId, ContainerFactory.EMPTY.get(), getPos(), RedstoneReceiverTileEntity.this)));

    public RedstoneReceiverTileEntity() {
        super(LogicBlockModule.TYPE_REDSTONE_RECEIVER.get());
    }

    public static RedstoneChannelBlock createBlock() {
        return new RedstoneChannelBlock(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .manualEntry(ManualHelper.create("rftoolsutility:logic/redstone_receiver"))
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header(), gold(),
                        parameter("channel", RedstoneChannelBlock::getChannelString))
                .tileEntitySupplier(RedstoneReceiverTileEntity::new));
    }

    public boolean getAnalog() {
        return analog;
    }

    public void setAnalog(boolean analog) {
        this.analog = analog;
        markDirtyClient();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            setRedstoneState(checkOutput());
        }
    }

    public int checkOutput() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(world);
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                int newout = ch.getValue();
                if(!analog && newout > 0) {
                    return 15;
                }
                return newout;
            }
        }
        return 0;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getInt("rs");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("rs", powerOutput);
        return tagCompound;
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        analog = info.getBoolean("analog");
    }

    @Override
    public void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putBoolean("analog", analog);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETANALOG.equals(command)) {
            setAnalog(params.get(ToggleButton.PARAM_ON));
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }

}
