package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ModAttachments {

  public static AttachmentType<List<CompoundTag>> STASHED_COMPANIONS;
  public static AttachmentType<Boolean> BOOK_AWAKENED;

  public static void register(BiConsumer<AttachmentType<?>, Identifier> consumer) {

    STASHED_COMPANIONS = AttachmentType.<List<CompoundTag>>builder(() -> List.of())
        .serialize(CompoundTag.CODEC.listOf().fieldOf("companions"), companions -> !companions.isEmpty())
        .build();
    consumer.accept(STASHED_COMPANIONS, Spookiness.id("stashed_companions"));

    BOOK_AWAKENED = AttachmentType.builder(() -> false)
        .serialize(Codec.BOOL.fieldOf("book_awakened"), awakened -> awakened)
        .sync(ByteBufCodecs.BOOL)
        .build();
    consumer.accept(BOOK_AWAKENED, Spookiness.id("book_awakened"));
  }
}
