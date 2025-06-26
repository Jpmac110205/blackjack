import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

public class GUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }

    static class Card {
        String rank, suit;
        Card(String rank, String suit) { this.rank = rank; this.suit = suit; }
        public String toString() { return rank + " of " + suit; }
    }

    // Game state
    private java.util.List<Card> deck;
    private java.util.List<Card> playerHand;
    private java.util.List<Card> dealerHand;
    private int dollars = 100;
    private int bet = 10; // Default bet
    private JLabel balanceLabel;
    private JTextField betField;
    private JLayeredPane dealerPanel;
    private java.util.List<ImagePanel> playerCardPanels = new ArrayList<>();

    // Dealer and Cards
    private JLabel dealer;
    private ImageIcon dealerNeutralIcon;
    private ImageIcon dealerWinIcon;
    private ImageIcon dealerLoseIcon;
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 150;
    private static final int CARD_SPACING = 20;
    private static final int START_X = 100;
    private static final int START_Y = 100;
    private static final int MAX_ROW_WIDTH = 700;

    // GUI components
    private JButton playAgain;

    // Helper to safely load images
    public ImageIcon safeLoadIcon(String path, int width, int height) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } else {
            System.err.println("Image not found: " + path);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(0, 0, width, height);
            g2.setColor(Color.RED);
            g2.drawString("Missing", 10, height / 2);
            g2.dispose();
            return new ImageIcon(img);
        }
    }

    public GUI() {
        // Dealer icons
        dealerNeutralIcon = safeLoadIcon("/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/Blackjack_dealer_neutral.png", 300, 400);
        dealerWinIcon = safeLoadIcon("/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/Blackjack_dealer_happy.png", 300, 400);
        dealerLoseIcon = safeLoadIcon("/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/Blackjack_dealer_sad.png", 300, 400);
        dealer = new JLabel(dealerNeutralIcon);

        // Main frame and panels
        JFrame frame = new JFrame("Blackjack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel bgPanel = new JPanel() {
            private final Image bg = safeLoadIcon("/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/background.jpg", 900, 700).getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        bgPanel.setLayout(new BorderLayout());

        dealerPanel = new JLayeredPane();
        dealerPanel.setOpaque(false);
        dealerPanel.setPreferredSize(new Dimension(900, 400));
        dealer.setBounds(300, 0, 300, 400); // Centered in the panel
        dealerPanel.add(dealer, Integer.valueOf(0)); // Background layer

        dealerPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int panelWidth = dealerPanel.getWidth();
                int panelHeight = dealerPanel.getHeight();
                int dealerWidth = dealer.getWidth();
                int dealerHeight = dealer.getHeight();
                dealer.setBounds(
                    (panelWidth - dealerWidth) / 2,
                    (panelHeight - dealerHeight) / 2,
                    dealerWidth,
                    dealerHeight
                );
            }
        });

        // "Your Cards:" label
        JLabel yourCardsLabel = new JLabel("Your Cards:");
        yourCardsLabel.setVisible(false);
        yourCardsLabel.setFont(new Font("Arial", Font.BOLD, 22));
        yourCardsLabel.setForeground(Color.BLACK);
        yourCardsLabel.setBounds(START_X, START_Y - 40, 300, 30);
        dealerPanel.add(yourCardsLabel, Integer.valueOf(2));

        // Controls at the bottom
        balanceLabel = new JLabel("Balance: $" + dollars, SwingConstants.CENTER);
        balanceLabel.setForeground(Color.BLACK);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 24));
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        betField = new JTextField(String.valueOf(bet), 5);
        betField.setMaximumSize(new Dimension(100, 40));
        betField.setFont(new Font("Arial", Font.PLAIN, 24));
        JLabel betLabel = new JLabel("Enter Bet: ");
        betLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        betLabel.setForeground(Color.BLACK);
        betLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button = new JButton("Start Game");
        button.setPreferredSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel("Welcome to Blackjack!", SwingConstants.CENTER);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.BOLD, 32));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton hitButton = new JButton("Hit");
        hitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton standButton = new JButton("Stand");
        standButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Place your bet!", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playAgain = new JButton("Play Again");
        playAgain.setVisible(false);
        playAgain.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hide game controls until game starts
        hitButton.setVisible(false);
        standButton.setVisible(false);
        statusLabel.setVisible(false);

        JPanel fgPanel = new JPanel();
        fgPanel.setOpaque(false);
        fgPanel.setLayout(new BoxLayout(fgPanel, BoxLayout.Y_AXIS));

        fgPanel.add(Box.createVerticalStrut(20));
        fgPanel.add(balanceLabel);
        fgPanel.add(Box.createVerticalStrut(10));

        JPanel betPanel = new JPanel();
        betPanel.setOpaque(false);
        betPanel.setLayout(new BoxLayout(betPanel, BoxLayout.X_AXIS));
        betPanel.add(betLabel);
        betPanel.add(betField);
        betPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fgPanel.add(betPanel);

        fgPanel.add(Box.createVerticalStrut(20));
        fgPanel.add(label);
        fgPanel.add(Box.createVerticalStrut(30));
        fgPanel.add(button);
        fgPanel.add(Box.createVerticalStrut(20));
        fgPanel.add(hitButton);
        fgPanel.add(Box.createVerticalStrut(10));
        fgPanel.add(standButton);
        fgPanel.add(Box.createVerticalStrut(30));
        fgPanel.add(statusLabel);
        fgPanel.add(Box.createVerticalStrut(20));
        fgPanel.add(playAgain);
        fgPanel.add(Box.createVerticalGlue());

        bgPanel.add(dealerPanel, BorderLayout.CENTER);
        bgPanel.add(fgPanel, BorderLayout.SOUTH);

        frame.setContentPane(bgPanel);
        frame.setSize(900, 700);
        frame.setMinimumSize(new Dimension(600, 500));
        frame.setVisible(true);

        // Button actions
        button.addActionListener(e -> {
            yourCardsLabel.setVisible(true);
            String betText = betField.getText().trim();
            try {
                int enteredBet = Integer.parseInt(betText);
                if (enteredBet <= 0 || enteredBet > dollars) {
                    statusLabel.setText("Invalid bet! Enter $1 to $" + dollars);
                    statusLabel.setVisible(true);
                    return;
                }
                bet = enteredBet;
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid number.");
                statusLabel.setVisible(true);
                return;
            }
            button.setVisible(false);
            betField.setEnabled(false);
            hitButton.setVisible(true);
            standButton.setVisible(true);
            statusLabel.setVisible(true);
            startNewGame(label, statusLabel, hitButton, standButton);
        });

        hitButton.addActionListener(e -> {
            playerHand.add(drawCard(deck));
            layoutPlayerCards();
            updateHands(label, statusLabel, false);
            if (handValue(playerHand) > 21) {
                statusLabel.setText("You busted! Dealer wins.");
                dealer.setIcon(dealerWinIcon);
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
                playAgain.setVisible(true);
                dollars -= bet;
                balanceLabel.setText("Balance: $" + dollars);
                checkGameOver(hitButton, standButton, playAgain, statusLabel, button, betField);
            }
        });

        playAgain.addActionListener(e -> {
            // Hide all card images
            for (ImagePanel panel : playerCardPanels) {
                dealerPanel.remove(panel);
            }
            yourCardsLabel.setVisible(false);
            playerCardPanels.clear();
            dealerPanel.repaint();

            // Hide last game's info
            playAgain.setVisible(false);
            statusLabel.setVisible(false);
            label.setText("Welcome to Blackjack!");
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            hitButton.setVisible(false);
            standButton.setVisible(false);
            dealer.setIcon(dealerNeutralIcon);
            betField.setEnabled(true);
            button.setVisible(true);
            statusLabel.setText("Place your bet!");
            statusLabel.setVisible(true);
        });

        standButton.addActionListener(e -> {
            hitButton.setEnabled(false);
            standButton.setEnabled(false);

            javax.swing.Timer dealerTimer = new javax.swing.Timer(700, null);
            dealerTimer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (handValue(dealerHand) < 17) {
                        dealerHand.add(drawCard(deck));
                        updateHands(label, statusLabel, true);
                    } else {
                        ((javax.swing.Timer) evt.getSource()).stop();
                        updateHands(label, statusLabel, true);
                        int playerTotal = handValue(playerHand);
                        int dealerTotal = handValue(dealerHand);
                        if (dealerTotal > 21 || playerTotal > dealerTotal) {
                            statusLabel.setText("You win!");
                            dealer.setIcon(dealerLoseIcon);
                            dollars += bet;
                        } else if (playerTotal < dealerTotal) {
                            statusLabel.setText("Dealer wins!");
                            dealer.setIcon(dealerWinIcon);
                            dollars -= bet;
                        } else {
                            statusLabel.setText("Push! It's a tie.");
                            dealer.setIcon(dealerNeutralIcon);
                        }
                        balanceLabel.setText("Balance: $" + dollars);
                        playAgain.setVisible(true);
                        checkGameOver(hitButton, standButton, playAgain, statusLabel, button, betField);
                    }
                }
            });
            dealerTimer.setInitialDelay(0);
            dealerTimer.start();
        });
    }

    // Helper methods for game logic
    private void startNewGame(JLabel label, JLabel statusLabel, JButton hitButton, JButton standButton) {
        statusLabel.setVisible(true);
        deck = createDeck();
        Collections.shuffle(deck);
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();
        playerHand.add(drawCard(deck));
        playerHand.add(drawCard(deck));
        dealerHand.add(drawCard(deck));
        dealerHand.add(drawCard(deck));
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        dealer.setIcon(dealerNeutralIcon);
        updateHands(label, statusLabel, false);
        balanceLabel.setText("Balance: $" + dollars);
        layoutPlayerCards();
    }

    private void layoutPlayerCards() {
        for (ImagePanel panel : playerCardPanels) {
            dealerPanel.remove(panel);
        }
        playerCardPanels.clear();

        int x = START_X;
        int y = START_Y;

        for (int i = 0; i < playerHand.size(); i++) {
            if (i > 0 && x + CARD_WIDTH > 600) {
                x = START_X;
                y += CARD_HEIGHT + CARD_SPACING;
            }
            Card card = playerHand.get(i);
            String imagePath = null;
            if (card.suit.equals("Hearts")) {
                imagePath = "/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/hearts.jpg";
            } else if (card.suit.equals("Spades")) {
                imagePath = "/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/spades.jpg";
            } else if (card.suit.equals("Clubs")) {
                imagePath = "/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/clubs.jpg";
            } else if (card.suit.equals("Diamonds")) {
                imagePath = "/Users/jpmac1102/Desktop/Visual Studio Projects/BlackJack/diamonds.jpg";
            }
            if (imagePath != null) {
                ImagePanel panel = new ImagePanel(imagePath, CARD_WIDTH, CARD_HEIGHT, card.rank, card.suit, this);
                panel.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);
                dealerPanel.add(panel, Integer.valueOf(1));
                playerCardPanels.add(panel);
            }
            x += CARD_WIDTH + CARD_SPACING;
        }
        dealerPanel.repaint();
    }

    private void updateHands(JLabel label, JLabel statusLabel, boolean showDealer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Your hand: ").append(handToString(playerHand))
          .append(" (Total: ").append(handValue(playerHand)).append(")<br>");
        if (showDealer) {
            sb.append("Dealer's hand: ").append(handToString(dealerHand))
              .append(" (Total: ").append(handValue(dealerHand)).append(")");
        } else {
            sb.append("Dealer shows: ").append(dealerHand.get(0)).append(" and a hidden card.");
        }
        sb.append("</html>");
        label.setText(sb.toString());
        statusLabel.setText("Hit or Stand?");
    }

    private java.util.List<Card> createDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        java.util.List<Card> deck = new ArrayList<>();
        for (String suit : suits)
            for (String rank : ranks)
                deck.add(new Card(rank, suit));
        return deck;
    }

    private Card drawCard(java.util.List<Card> deck) {
        return deck.remove(deck.size() - 1);
    }

    private int handValue(java.util.List<Card> hand) {
        int value = 0, aceCount = 0;
        for (Card card : hand) {
            switch (card.rank) {
                case "A": value += 11; aceCount++; break;
                case "K": case "Q": case "J": value += 10; break;
                default: value += Integer.parseInt(card.rank);
            }
        }
        while (value > 21 && aceCount > 0) { value -= 10; aceCount--; }
        return value;
    }

    private String handToString(java.util.List<Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(hand.get(i));
            if (i < hand.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void checkGameOver(JButton hitButton, JButton standButton, JButton playAgain, JLabel statusLabel, JButton button, JTextField betField) {
        if (dollars <= 0) {
            statusLabel.setText("You're out of money! Game over.");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            playAgain.setVisible(false);
            betField.setEnabled(false);
            button.setEnabled(false);
        }
    }
}

// Card image panel with value overlay
class ImagePanel extends JPanel {
    private final Image img;
    private final String rank;
    private final String suit;
    private final Color valueColor;

    public ImagePanel(String imagePath, int width, int height, String rank, String suit, GUI gui) {
        this.img = gui.safeLoadIcon(imagePath, width, height).getImage();
        this.rank = rank;
        this.suit = suit;
        if (suit.equals("Hearts") || suit.equals("Diamonds")) {
            valueColor = Color.RED;
        } else {
            valueColor = Color.BLACK;
        }
        setOpaque(false);
        setSize(width, height);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
        g.setColor(valueColor);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String value = rank;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(value);
        int textHeight = fm.getAscent();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2;
        g.drawString(value, x, y);
    }
}