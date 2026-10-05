package io.github.jason13official.spookiness.companion;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface PlayerFollower {

  @Nullable UUID getOwnerUUID();
}
