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

    STASHED_COMPANIONS = AttachmentType.<List<CompoundTag>>builder(() -> List.of())
        .serialize(CompoundTag.CODEC.listOf().fieldOf("companions"), companions -> !companions.isEmpty())
        .build();
    consumer.accept(STASHED_COMPANIONS, Spookiness.id("stashed_companions"));

    BOOK_AWAKENED = AttachmentType.builder(() -> false)
        .serialize(Codec.BOOL.fieldOf("book_awakened"), awakened -> awakened)
        .sync(ByteBufCodecs.BOOL)
        .build();
    consumer.accept(BOOK_AWAKENED, Spookiness.id("book_awakened"));

    LAMENT_RITUAL = AttachmentType.builder(() -> 0)
        .sync(ByteBufCodecs.VAR_INT)
        .build();
    consumer.accept(LAMENT_RITUAL, Spookiness.id("lament_ritual"));

    HALLOWED_OWNER = AttachmentType.<Optional<UUID>>builder(Optional::empty)
        .serialize(UUIDUtil.CODEC.optionalFieldOf("owner"), Optional::isPresent)
        .sync(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC))
        .build();
    consumer.accept(HALLOWED_OWNER, Spookiness.id("hallowed_owner"));

    MOTHER_NIGHT = AttachmentType.builder(() -> -1L)
        .serialize(Codec.LONG.fieldOf("mother_night"), night -> night >= 0L)
        .copyOnDeath()
        .build();
    consumer.accept(MOTHER_NIGHT, Spookiness.id("mother_night"));

    KINDLE = AttachmentType.builder(() -> new Kindling.Kindle(Util.NIL_UUID, Wickman.Variant.WICK, 0))
        .serialize(Kindling.Kindle.CODEC.fieldOf("kindle"))
        .build();
    consumer.accept(KINDLE, Spookiness.id("kindle"));
  }
}
