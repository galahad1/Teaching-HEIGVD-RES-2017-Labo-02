package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements the client side of the protocol specification (version 1).
 *
 * @author Olivier Liechti
 * @autor Loan Lassalle
 * @author Tano Iannetta
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

  private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

  /**
   * @brief socket Socket connection's interface of server
   */
  private Socket socket;

  /**
   * @brief in BufferedReader input stream to read the data sent by of server
   */
  private BufferedReader in;

  /**
   * @brief out PrintWriter output stream to write the data to send to server
   */
  private PrintWriter out;

  /**
   * @brief connect connection to targeted server and creation of input and output stream
   * @param server String address of targeted server
   * @param port int port number of targeted server
   * @throws IOException with Socket, BufferedReader and PrintWriter
   */
  @Override
  public void connect(String server, int port) throws IOException {
    //Opens socket
    socket = new Socket(server, port);

    // Opens input and output streams
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(socket.getOutputStream());

    // Wait a message of targeted server
    receive();
  }

  /**
   * @brief disconnect disconnection of targeted server
   * @throws IOException with closing of BufferedReader, PrintWriter and Socket
   */
  @Override
  public void disconnect() throws IOException {
    // Informs the server to close the connection
    send(RouletteV1Protocol.CMD_BYE);

    // Closes streams and socket
    in.close();
    out.close();
    socket.close();

    // Display a message of disconnection
    LOG.info("Disconnected");
  }

  /**
   * @brief boolean isConnected indicates if TCP client is connected
   * @return True if TCP client is connected, false otherwies.
   */
  @Override
  public boolean isConnected() {
    return socket != null && socket.isConnected();
  }

  /**
   * @brief String receives get back message of targeted server
   * @return message received
   * @throws IOException with BufferedReader
   */
  public String receive() throws IOException {
    return in.readLine();
  }

  /**
   * @brief send sends a message to targeted server
   * @param message String message to send to targeted server
   * @throws IOException with PrintWriter
   */
  public void send(String message) throws IOException {
    out.println(message);
    out.flush();
  }

  /**
   * @brief loadStudent sends the student's full name
   * @param fullname String student's full name
   * @throws IOException with PrintWriter and BufferedReader
   */
  @Override
  public void loadStudent(String fullname) throws IOException {
    send(RouletteV1Protocol.CMD_LOAD);
    receive();

    send(fullname);

    send(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
    receive();
  }

  /**
   * @brief loadStudent sends a list of full name of students
   * @param students List<Student> list of full name of students
   * @throws IOException with PrintWriter and BufferedReader
   */
  @Override
  public void loadStudents(List<Student> students) throws IOException {
    send(RouletteV1Protocol.CMD_LOAD);
    receive();

    for (Student s : students) {
      send(s.getFullname());
    }

    send(RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER);
    receive();
  }

  /**
   * @brief Student pickRandomStudent get back a randomly student
   * @return random student
   * @throws EmptyStoreException with RandomCommandResponse
   * @throws IOException with PrintWriter and BufferedReader
   */
  @Override
  public Student pickRandomStudent() throws EmptyStoreException, IOException {
    send(RouletteV1Protocol.CMD_RANDOM);

    RandomCommandResponse responseRandom = JsonObjectMapper.parseJson(receive(), RandomCommandResponse.class);

    if (!responseRandom.getError().isEmpty())
    {
      throw new EmptyStoreException();
    }

    return new Student(responseRandom.getFullname());
  }

  /**
   * @brief int getNumberOfStudents get back number of students
   * @return number of students
   * @throws IOException with PrintWriter and BufferedReader
   */
  @Override
  public int getNumberOfStudents() throws IOException {
    send(RouletteV1Protocol.CMD_INFO);

    InfoCommandResponse responseInfo = JsonObjectMapper.parseJson(receive(), InfoCommandResponse.class);

    return responseInfo.getNumberOfStudents();
  }

  /**
   * @brief String getProtocolVersion get back protocol version of targeted server
   * @return protocol version of targeted server
   * @throws IOException with PrintWriter and BufferedReader
   */
  @Override
  public String getProtocolVersion() throws IOException {
    send(RouletteV1Protocol.CMD_INFO);

    InfoCommandResponse responseInfo = JsonObjectMapper.parseJson(receive(), InfoCommandResponse.class);

    return responseInfo.getProtocolVersion();
  }
}