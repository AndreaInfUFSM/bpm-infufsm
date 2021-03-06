/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.bonitasoft.connectors.ws.cxf;

import java.io.IOException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;

public class Server {

  private ServerThread thread;

  public Server() throws IOException {
    this(9002);
  }
  
  public Server(final int connectorPort) throws IOException {
    final org.mortbay.jetty.Server server = new org.mortbay.jetty.Server();
    
    final SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(connectorPort);
    server.setConnectors(new Connector[] {connector});

    WebAppContext webappcontext = new WebAppContext();
    webappcontext.setContextPath("/");

    webappcontext.setWar("src/test/resources/webapp");

    HandlerCollection handlers = new HandlerCollection();
    handlers.setHandlers(new Handler[] {webappcontext, new DefaultHandler()});

    server.setHandler(handlers);

    HashUserRealm myrealm = new HashUserRealm("MyRealm","src/test/resources/org/bonitasoft/connectors/ws/cxf/realm.properties");
    server.setUserRealms(new UserRealm[]{myrealm});
    
    this.thread = new ServerThread(server);
  }
  
  public void start() throws Exception {
    this.thread.start();
    System.err.println("Starting server...");
    do {
      System.err.println("Waiting...");
      Thread.sleep(200);
    } while (!this.thread.isServerStarted());
    System.err.println("Server started.");
    System.err.println("Waiting for WS to be deployed...");
    Thread.sleep(7000);
    System.err.println("Assuming WS are deployed.");
  }

  public void stop() {
    this.thread.shutdown();
  }

}
