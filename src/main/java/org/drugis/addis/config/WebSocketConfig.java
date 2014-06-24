package org.drugis.addis.config;

import org.drugis.addis.websocket.ClientHandler;
import org.drugis.addis.websocket.Handler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by daan on 24-6-14.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  StandardWebSocketClient myClient;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
    webSocketHandlerRegistry.addHandler(myHandler(), "/handler");
  }

  @Bean
  public WebSocketHandler myHandler() {
    return new Handler();
  }

  @Bean
  public WebSocketConnectionManager connectionManager() {

    System.out.println("socket client config");

    String url = "ws://localhost:3000/ws";
    WebSocketHandler handler = new ClientHandler();

    StandardWebSocketClient client = new StandardWebSocketClient();

    WebSocketConnectionManager manager =
      new WebSocketConnectionManager(client, handler, url);

    manager.setAutoStartup(true);
    return manager;
  }


}
