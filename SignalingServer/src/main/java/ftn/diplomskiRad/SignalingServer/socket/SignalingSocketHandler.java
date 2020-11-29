package ftn.diplomskiRad.SignalingServer.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.diplomskiRad.SignalingServer.model.SignalMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignalingSocketHandler extends TextWebSocketHandler {
    private static final String JOIN_ROOM = "join-room";
    private static final String ANSWER = "answer";
    private static final String OFFER = "offer";
    private static final String ICE_CANDIDATE = "ice-candidate";
    private static final String OTHER_USER = "other-user";
    private static final String USER_JOINED = "user-joined";

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, ArrayList<WebSocketSession>> rooms = new HashMap<String, ArrayList<WebSocketSession>>();

    @Override
    protected void handleTextMessage(final WebSocketSession session, TextMessage message) throws Exception {
        SignalMessage signalMessage = objectMapper.readValue(message.getPayload(), SignalMessage.class);

        if (JOIN_ROOM.equalsIgnoreCase(signalMessage.getType())) {
            String roomID = signalMessage.getRoomID();

            ArrayList<WebSocketSession> room = rooms.get(roomID);

            if (room == null) {
                ArrayList<WebSocketSession> sessions = new ArrayList<WebSocketSession>();
                sessions.add(session);
                rooms.put(roomID, sessions);
            } else {
                room.add(session);
            }
            if (room != null && room.size() == 2) {
                WebSocketSession other = room.stream().filter(s -> !s.getId().equals(session.getId())).findFirst().orElse(null);
                if (other != null) {
                    SignalMessage out = new SignalMessage();
                    out.setType(OTHER_USER);
                    out.setDest(other.getId());
                    String jsonOut = objectMapper.writeValueAsString(out);
                    session.sendMessage(new TextMessage(jsonOut));

                    SignalMessage otherOut = new SignalMessage();
                    otherOut.setType(USER_JOINED);
                    otherOut.setDest(session.getId());
                    String jsonOtherOut = objectMapper.writeValueAsString(otherOut);
                    other.sendMessage(new TextMessage(jsonOtherOut));
                }
            }
        }
        String roomID = signalMessage.getRoomID();
        String dest = signalMessage.getDest();
        WebSocketSession destSocket = rooms.get(roomID).stream().filter(s -> s.getId().equals(dest)).findFirst().orElse(null);

        if (destSocket != null && destSocket.isOpen()) {
            if (OFFER.equalsIgnoreCase(signalMessage.getType())) {
                SignalMessage out = new SignalMessage();
                out.setType(OFFER);
                out.setDest(signalMessage.getDest());
                out.setSource(session.getId());
                out.setData(signalMessage.getData());
                String stringifiedJSONmsg = objectMapper.writeValueAsString(out);
                destSocket.sendMessage(new TextMessage(stringifiedJSONmsg));
            }
            if (ANSWER.equalsIgnoreCase(signalMessage.getType())) {
                SignalMessage out = new SignalMessage();
                out.setType(ANSWER);
                out.setDest(signalMessage.getDest());
                out.setSource(session.getId());
                out.setData(signalMessage.getData());
                String stringifiedJSONmsg = objectMapper.writeValueAsString(out);
                destSocket.sendMessage(new TextMessage(stringifiedJSONmsg));
            }
            if (ICE_CANDIDATE.equalsIgnoreCase(signalMessage.getType())) {
                SignalMessage out = new SignalMessage();
                out.setType(ICE_CANDIDATE);
                out.setDest(signalMessage.getDest());
                out.setData(signalMessage.getData());
                String stringifiedJSONmsg = objectMapper.writeValueAsString(out);
                destSocket.sendMessage(new TextMessage(stringifiedJSONmsg));
            }
        }


    }
}
