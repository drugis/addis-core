package org.drugis.addis.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Created by daan on 24-6-14.
 */
public class ClientHandler extends TextWebSocketHandler{

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
      System.out.println("!!!!!!!!!!!!!!!! YO client");
      System.out.println("message : " + message.getPayload());
    }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println("!!!!!!!!!!!!!!!! connect to tha patavi client");
  }

}
