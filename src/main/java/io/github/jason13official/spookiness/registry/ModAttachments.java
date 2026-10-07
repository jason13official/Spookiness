package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.ritual.Kindling;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import net.minecraft.util.Util;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ModAttachments {

  public static AttachmentType<List<CompoundTag>> STASHED_COMPANIONS;
  public static AttachmentType<Boolean> BOOK_AWAKENED;
  public static AttachmentType<Integer> LAMENT_RITUAL;
  public static AttachmentType<Optional<UUID>> HALLOWED_OWNER;
  public static AttachmentType<Long> MOTHER_NIGHT;
  public static AttachmentType<Kindling.Kindle> KINDLE;

  public static void register(BiConsumer<AttachmentType<?>, Identifier> consumer) {

    STASHED_COMPANIONS = register(consumer, "stashed_companions", AttachmentType.<List<CompoundTag>>builder(() -> List.of())
        .serialize(CompoundTag.CODEC.listOf().fieldOf("companions"), companions -> !companions.isEmpty()));

    BOOK_AWAKENED = register(consumer, "book_awakened", AttachmentType.builder(() -> false)
        .serialize(Codec.BOOL.fieldOf("book_awakened"), awakened -> awakened)
        .sync(ByteBufCodecs.BOOL));

    LAMENT_RITUAL = register(consumer, "lament_ritual", AttachmentType.builder(() -> 0)
        .sync(ByteBufCodecs.VAR_INT));

    HALLOWED_OWNER = register(consumer, "hallowed_owner", AttachmentType.<Optional<UUID>>builder(Optional::empty)
        .serialize(UUIDUtil.CODEC.optionalFieldOf("owner"), Optional::isPresent)
        .sync(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)));

    MOTHER_NIGHT = register(consumer, "mother_night", AttachmentType.builder(() -> -1L)
        .serialize(Codec.LONG.fieldOf("mother_night"), night -> night >= 0L)
        .copyOnDeath());

    KINDLE = register(consumer, "kindle", AttachmentType.builder(() -> new Kindling.Kindle(Util.NIL_UUID, Wickman.Variant.WICK, 0))
        .serialize(Kindling.Kindle.CODEC.fieldOf("kindle")));
  }

  private static <T> AttachmentType<T> register(BiConsumer<AttachmentType<?>, Identifier> consumer, String name, AttachmentType.Builder<T> builder) {

    AttachmentType<T> type = builder.build();
    consumer.accept(type, Spookiness.id(name));
    return type;
  }
}
