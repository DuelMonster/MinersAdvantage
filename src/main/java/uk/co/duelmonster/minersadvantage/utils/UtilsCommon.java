package uk.co.duelmonster.minersadvantage.utils;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeBlock;

public class UtilsCommon {

  public static void checkNotEmpty(@Nullable
  ItemStack itemStack) {
    if (itemStack == null) {
      throw new NullPointerException("ItemStack must not be null.");

    } else if (itemStack.isEmpty()) {
      String info = getItemStackInfo(itemStack);
      throw new IllegalArgumentException("ItemStack value must not be empty. " + info);
    }
  }

  public static void checkNotEmpty(@Nullable
  ItemStack itemStack, String name) {
    if (itemStack == null) {
      throw new NullPointerException(name + " must not be null.");

    } else if (itemStack.isEmpty()) {
      String info = getItemStackInfo(itemStack);
      throw new IllegalArgumentException("ItemStack " + name + " must not be empty. " + info);
    }
  }

  public static <T> void checkNotEmpty(@Nullable
  T[] values, String name) {
    if (values == null)
      throw new NullPointerException(name + " must not be null.");

    else if (values.length <= 0)
      throw new IllegalArgumentException(name + " must not be empty.");

    for (T value : values)
      if (value == null)
        throw new NullPointerException(name + " must not contain null values.");

  }

  public static void checkNotEmpty(@Nullable
  Collection<?> values, String name) {
    if (values == null)
      throw new NullPointerException(name + " must not be null.");

    else if (values.isEmpty())
      throw new IllegalArgumentException(name + " must not be empty.");

    else if (!(values instanceof NonNullList))
      for (Object value : values)
      if (value == null)
        throw new NullPointerException(name + " must not contain null values.");
  }

  public static <T> void checkNotNull(@Nullable
  T object, String name) {
    if (object == null)
      throw new NullPointerException(name + " must not be null.");
  }

  public static String getItemStackInfo(@Nullable
  ItemStack itemStack) {
    if (itemStack == null)
      return "null";

    Item             item         = itemStack.getItem();
    final String     itemName;
    ResourceLocation registryName = item.getRegistryName();

    if (registryName != null) {
      itemName = registryName.toString();

    } else if (item instanceof IForgeBlock) {
      final String blockName;
      Block        block = ((IForgeBlock) item).getBlock();

      if (block == null) {
        blockName = "null";
      } else {
        ResourceLocation blockRegistryName = block.getRegistryName();

        if (blockRegistryName != null)
          blockName = blockRegistryName.toString();
        else
          blockName = block.getClass().getName();

      }

      itemName = "ItemBlock(" + blockName + ")";

    } else
      itemName = item.getClass().getName();

    CompoundNBT nbt = itemStack.getTag();
    if (nbt != null)
      return itemStack + " " + itemName + " nbt:" + nbt;

    return itemStack + " " + itemName;
  }
}