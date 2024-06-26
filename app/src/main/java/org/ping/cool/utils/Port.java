package org.ping.cool.utils;

import android.util.Log;

import org.ping.cool.utils.logger.Color;
import org.ping.cool.utils.logger.Logger;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * This class is used to handle the scanned ports.
 */
public class Port implements Comparable<Port> {

  /**
   * Scanned port
   */
  private final int port;

  /**
   * This variable is used to show whether a connection
   * has been established to this port.
   */
  private boolean isOpen;
  private String portService;
  private String portProtocol;
  private String statePort = "close";

  public Port(int port,String protocol ,String service,boolean isOpen) {
    this.port = port;
    setOpen(isOpen);
    setPortProtocol(protocol);
    setPortService(service);

  }

  /**
   * This function is used to scan an actual port
   *
   * @param es      ExecutorService
   * @param ip      IPV4 address of the server
   * @param port    port that needs to be scanned
   * @param timeout time before the connection gets closed
   * @return a port object
   */
  public static Future<Port> scan(final ExecutorService es, final String ip, final int port, final int timeout) {
    return es.submit(new Callable<Port>() {
      @Override
      public Port call() {
        try {
          // Socket initialization
          Socket socket = new Socket();
          // The socket attempts to establish a connection with the server
          socket.connect(new InetSocketAddress(ip, port), timeout);
          // The socket closes the connection. If the program reaches this point it means that the port is open
          socket.close();

          String portService = WellKnownPort.getByPortNumber(port).getName();

          if (!portService.toLowerCase().equals("unknown"))
            Logger.log(Color.GREEN.getColor() + "Port " + Color.CYAN.getColor() + port + Color.GREEN.getColor() + " [" + Color.CYAN.getColor() + portService +  Color.GREEN.getColor() + "] seems to be reachable!");
          else
            Logger.log(Color.GREEN.getColor() + "Port " + Color.CYAN.getColor() + port + Color.GREEN.getColor() + " seems to be reachable!");

          // Returning the port and the parameter "isOpen" is set to true.
          return new Port(port, WellKnownPort.getByPortNumber(port).getProtocol(),WellKnownPort.getByPortNumber(port).getName(),true);
        } catch (Exception ex) {
          // This happens when the connection has not been successful.
          // Returning the port and the parameter "isOpen" is set to false.
          return new Port(port, WellKnownPort.getByPortNumber(port).getProtocol(),WellKnownPort.getByPortNumber(port).getName(),false);
        }
      }
    });
  }

  public int getPort() {
    return port;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(boolean open) {
    isOpen = open;
    if(open){
      setStatePort("open");
    }else{
      setStatePort("close");
    }
  }

  public String getPortService() {
    return portService;
  }

  public void setPortService(String portService) {
    this.portService = portService;
  }

  public String getPortProtocol() {
    return portProtocol;
  }

  public void setPortProtocol(String portProtocol) {
    this.portProtocol = portProtocol;
  }
  public String getStatePort() {
    return statePort;
  }

  public void setStatePort(String statePort) {
    this.statePort = statePort;
  }
  @Override
  public int compareTo(Port o) {

    if (getStatePort() == null || o.getStatePort() == null)
      return 0;
    return getStatePort().compareTo(o.getStatePort());
  }
}
