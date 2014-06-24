package org.drugis.addis.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.inject.Inject;

public class Handler extends TextWebSocketHandler {

  @Inject
  WebSocketConnectionManager myHandler;

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    System.out.println("!!!!!!!!!!!!!!!! YO message");
    System.out.println("message : " + message.getPayload());
  }



}
