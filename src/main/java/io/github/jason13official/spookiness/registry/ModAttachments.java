package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ModAttachments {

  public static AttachmentType<List<CompoundTag>> STASHED_COMPANIONS;

  public static void register(BiConsumer<AttachmentType<?>, Identifier> consumer) {

    STASHED_COMPANIONS = AttachmentType.<List<CompoundTag>>builder(() -> List.of())
        .serialize(CompoundTag.CODEC.listOf().fieldOf("companions"), companions -> !companions.isEmpty())
        .build();
    consumer.accept(STASHED_COMPANIONS, Spookiness.id("stashed_companions"));
  }
}
