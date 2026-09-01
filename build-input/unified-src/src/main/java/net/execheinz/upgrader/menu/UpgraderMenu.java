package net.execheinz.upgrader.menu;

import net.execheinz.upgrader.registry.ModItems;
import net.execheinz.upgrader.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class UpgraderMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    private final Container input = new SimpleContainer(1);

    public UpgraderMenu(int id, Inventory inventory, FriendlyByteBuf ignored) { this(id, inventory); }

    public UpgraderMenu(int id, Inventory inventory) {
        super(ModMenus.UPGRADER.get(), id);

        addSlot(new Slot(input, 0, StationLayout.INPUT_X, StationLayout.INPUT_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return !stack.is(ModItems.UPGRADER.get()) && !stack.is(ModItems.CASE_DISPLAY.get());
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                    inventory,
                    col + row * 9 + 9,
                    StationLayout.INVENTORY_X + col * 18,
                    StationLayout.INVENTORY_Y + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, StationLayout.INVENTORY_X + col * 18, StationLayout.HOTBAR_Y));
        }
    }

    public ItemStack getInputStack() { return input.getItem(0); }

    public void setInputStack(ItemStack stack) {
        input.setItem(0, stack);
        input.setChanged();
        broadcastChanges();
    }

    public void markInputChanged() {
        input.setChanged();
        broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (!player.isAlive()) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.UPGRADER.get())) return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (getSlot(INPUT_SLOT).hasItem()) return ItemStack.EMPTY;
            if (!getSlot(INPUT_SLOT).mayPlace(stack)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) clearContainer(player, input);
    }
}
