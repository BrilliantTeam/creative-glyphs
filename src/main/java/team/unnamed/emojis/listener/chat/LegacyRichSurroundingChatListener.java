package team.unnamed.emojis.listener.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import team.unnamed.emojis.EmojiRegistry;
import team.unnamed.emojis.format.EmojiComponentProvider;
import team.unnamed.emojis.format.EmojiReplacer;
import team.unnamed.emojis.listener.EventCancellationStrategy;
import team.unnamed.emojis.listener.EventListener;

/**
 * Class listening for {@link AsyncPlayerChatEvent} that
 * cancels the event and manually sends the component to
 * the event recipients
 */
public class LegacyRichSurroundingChatListener
        implements EventListener<AsyncPlayerChatEvent> {

    private final EmojiRegistry emojiRegistry;
    private final EmojiComponentProvider emojiComponentProvider;
    private final EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy;

    public LegacyRichSurroundingChatListener(
            EmojiRegistry emojiRegistry,
            EmojiComponentProvider emojiComponentProvider,
            EventCancellationStrategy<AsyncPlayerChatEvent> cancellationStrategy
    ) {
        this.emojiRegistry = emojiRegistry;
        this.emojiComponentProvider = emojiComponentProvider;
        this.cancellationStrategy = cancellationStrategy;
    }

    @Override
    public Class<AsyncPlayerChatEvent> getEventType() {
        return AsyncPlayerChatEvent.class;
    }

    @Override
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        BaseComponent[] translated = EmojiReplacer.replaceRawToRich(
                player,
                emojiRegistry,
                String.format(event.getFormat(), player.getName(), message),
                emojiComponentProvider
        );

        for (Player recipient : event.getRecipients()) {
            recipient.spigot().sendMessage(translated);
        }

        cancellationStrategy.surround(event);
    }

}
