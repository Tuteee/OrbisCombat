package net.earthmc.emccom.combat.listener;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CombatLogMessages {

    private final List<String> messages;
    private final Random random;

    public CombatLogMessages(Random random, List<String> messages) {
        if (random == null || messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Random and message list must not be null or empty");
        }
        this.random = (random != null) ? random : new Random();
        this.messages = messages;
    }

    public String getRandomMessage() {
        int randomIndex = random.nextInt(messages.size());
        return messages.get(randomIndex);
    }

    /*public static void main(String[] args) {
        // Example usage
        List<String> messagesList = Arrays.asList(
                "used Combat Log! It's a One-Hit KO!",
                "was killed for logging out in combat.",
                "encountered connection issues and DIED.",
                "surrendered to the disconnect button.",
                "tried fleeing the battle and failed."
        );

        Random random = new Random();
        CombatLogMessages messageSelector = new CombatLogMessages(random, messagesList);

        // Get and print a random message
        String randomMessage = messageSelector.getRandomMessage();
        System.out.println("Random Message: " + randomMessage);
    }*/
}
