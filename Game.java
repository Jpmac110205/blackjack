import java.util.*;

public class Game {
    private int dollars = 100;
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        while (dollars > 0) {
            System.out.println("You have $" + dollars + ". Place your bet:");
            int bet = getValidBet();

            // Initialize and shuffle deck
            List<Card> deck = createDeck();
            Collections.shuffle(deck);

            // Deal initial cards
            List<Card> playerHand = new ArrayList<>();
            List<Card> dealerHand = new ArrayList<>();
            playerHand.add(drawCard(deck));
            playerHand.add(drawCard(deck));
            dealerHand.add(drawCard(deck));
            dealerHand.add(drawCard(deck));

            System.out.println("You have: " + handToString(playerHand) + " (Total: " + handValue(playerHand) + ")");
            System.out.println("The dealer has: " + dealerHand.get(0) + " and a hidden card.");

            // Player's turn
            boolean playerBusted = false;
            while (true) {
                int playerTotal = handValue(playerHand);
                if (playerTotal > 21) {
                    System.out.println("You busted!");
                    playerBusted = true;
                    break;
                }
                System.out.println("Your total is " + playerTotal + ". Do you want to hit? (y/n)");
                String choice = getValidChoice();
                if (choice.equalsIgnoreCase("y")) {
                    playerHand.add(drawCard(deck));
                    System.out.println("You drew: " + playerHand.get(playerHand.size() - 1));
                    System.out.println("Your hand: " + handToString(playerHand) + " (Total: " + handValue(playerHand) + ")");
                } else {
                    break;
                }
            }

            // Dealer's turn
            boolean dealerBusted = false;
            if (!playerBusted) {
                System.out.println("Dealer's hand: " + handToString(dealerHand) + " (Total: " + handValue(dealerHand) + ")");
                while (handValue(dealerHand) < 17) {
                    Card newCard = drawCard(deck);
                    dealerHand.add(newCard);
                    System.out.println("Dealer drew: " + newCard);
                    System.out.println("Dealer's hand: " + handToString(dealerHand) + " (Total: " + handValue(dealerHand) + ")");
                }
                if (handValue(dealerHand) > 21) {
                    System.out.println("Dealer busted!");
                    dealerBusted = true;
                } else {
                    System.out.println("Dealer stands with " + handValue(dealerHand));
                }
            }

            // Determine winner
            int playerTotal = handValue(playerHand);
            int dealerTotal = handValue(dealerHand);
            if (playerBusted) {
                dollars -= bet;
                System.out.println("You lose. You now have $" + dollars);
            } else if (dealerBusted) {
                dollars += bet;
                System.out.println("Dealer busts. You win! You now have $" + dollars);
            } else if (playerTotal > dealerTotal) {
                dollars += bet;
                System.out.println("You win! You now have $" + dollars);
            } else if (playerTotal < dealerTotal) {
                dollars -= bet;
                System.out.println("You lose. You now have $" + dollars);
            } else {
                System.out.println("Push! It's a tie. You keep your bet. You still have $" + dollars);
            }

            // Ask to play again
            System.out.println("Play again? (y/n)");
            String playAgain = getValidChoice();
            if (!playAgain.equalsIgnoreCase("y")) {
                break;
            }
        }
        System.out.println("Game over. You finished with $" + dollars);
    }

    // Card class
    static class Card {
        String rank, suit;
        Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }
        public String toString() {
            return rank + " of " + suit;
        }
    }

    // Create a standard deck
    private List<Card> createDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        List<Card> deck = new ArrayList<>();
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(rank, suit));
            }
        }
        return deck;
    }

    // Draw a card from the deck
    private Card drawCard(List<Card> deck) {
        return deck.remove(deck.size() - 1);
    }

    // Calculate hand value with Ace logic
    private int handValue(List<Card> hand) {
        int value = 0;
        int aceCount = 0;
        for (Card card : hand) {
            switch (card.rank) {
                case "A":
                    value += 11;
                    aceCount++;
                    break;
                case "K": case "Q": case "J":
                    value += 10;
                    break;
                default:
                    value += Integer.parseInt(card.rank);
            }
        }
        // Adjust for Aces if bust
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        return value;
    }

    // Convert hand to string
    private String handToString(List<Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(hand.get(i));
            if (i < hand.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    // Get a valid bet from the user
    private int getValidBet() {
        int bet = 0;
        while (true) {
            try {
                bet = Integer.parseInt(scanner.next());
                if (bet > 0 && bet <= dollars) break;
                System.out.println("Invalid bet. Enter a value between 1 and " + dollars + ":");
            } catch (Exception e) {
                System.out.println("Please enter a valid number:");
                scanner.nextLine();
            }
        }
        return bet;
    }

    // Get a valid choice (y/n)
    private String getValidChoice() {
        while (true) {
            String input = scanner.next();
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n")) return input;
            System.out.println("Please enter 'y' or 'n':");
        }
    }
}