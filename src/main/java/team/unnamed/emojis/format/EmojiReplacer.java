package team.unnamed.emojis.format;

import org.bukkit.permissions.Permissible;
import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.EmojiRegistry;

/**
 * Utility class for replacing emojis in strings
 * @author yusshu (Andre Roldan)
 */
public class EmojiReplacer {

    /**
     * Replaces the emojis in the given {@code text}
     * if the given {@code permissible} has permission
     * to use them
     */
    public static String replace(
            Permissible permissible,
            EmojiRegistry registry,
            CharSequence text
    ) {

        StringBuilder builder = new StringBuilder();
        StringBuilder name = new StringBuilder();

        textLoop:
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':') {
                while (++i < text.length()) {
                    char current = text.charAt(i);
                    if (current == ':') {
                        if (name.length() < 1) {
                            builder.append(':');
                            continue;
                        }
                        String nameStr = name.toString();
                        Emoji emoji = registry.get(nameStr);
                        if (!Permissions.canUse(permissible, emoji)) {
                            builder
                                    .append(':')
                                    .append(nameStr);
                            name.setLength(0);
                            continue;
                        } else {
                            builder.append(emoji.getCharacter());
                        }
                        name.setLength(0);
                        continue textLoop;
                    } else {
                        name.append(current);
                    }
                }
                builder.append(':').append(name);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}