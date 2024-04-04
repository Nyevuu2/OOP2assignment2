import java.io.IOException;
import java.net.*;

public class ElectionVoteCasting {

    private static final String MULTICAST_GROUP = "224.3.29.71";
    private static final int PORT = 10000;
    private static final int NUM_ELECTORATES = 5;

    private static int votesA = 0;
    private static int votesB = 0;

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            @SuppressWarnings("resource")
            MulticastSocket socket = new MulticastSocket(PORT);
            socket.joinGroup(group);

            Thread receiverThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String vote = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Received vote: " + vote);
                        updateVoteCount(vote);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiverThread.start();

            for (int i = 0; i < NUM_ELECTORATES; i++) {
                String vote = askForVote();
                byte[] voteBytes = vote.getBytes();
                DatagramPacket packet = new DatagramPacket(voteBytes, voteBytes.length, group, PORT);
                socket.send(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void updateVoteCount(String vote) {
        if (vote.equals("A")) {
            votesA++;
        } else if (vote.equals("B")) {
            votesB++;
        }
        String winner = (votesA > votesB) ? "A" : (votesB > votesA) ? "B" : "Tie";
        System.out.println("Current winner: " + winner);
    }

    @SuppressWarnings("resource")
    private static String askForVote() {
        String vote = "";
        boolean validVote = false;
        while (!validVote) {
            System.out.print("Cast your vote (A/B): ");
            try {
                vote = new java.util.Scanner(System.in).nextLine().toUpperCase();
                if (vote.equals("A") || vote.equals("B")) {
                    validVote = true;
                } else {
                    System.out.println("Invalid vote. Please enter 'A' or 'B'.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
        return vote;
    }
}
