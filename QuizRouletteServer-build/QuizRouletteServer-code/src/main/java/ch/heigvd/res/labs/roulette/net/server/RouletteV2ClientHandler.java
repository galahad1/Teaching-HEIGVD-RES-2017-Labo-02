package ch.heigvd.res.labs.roulette.net.server;

import ch.heigvd.res.labs.roulette.data.*;
import ch.heigvd.res.labs.roulette.net.protocol.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Roulette protocol (version 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientHandler  implements IClientHandler {

  final static Logger LOG = Logger.getLogger(RouletteV2ClientHandler.class.getName());
  private final IStudentsStore store;
  private int nbrCommands;


  public RouletteV2ClientHandler(IStudentsStore store) {
    this.store = store;
  }

  @Override
  public void handleClientConnection(InputStream is, OutputStream os) throws IOException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));

    writer.println("Hello. Online HELP is available. Will you find it?");
    writer.flush();

    String command;
    boolean done = false;
    while (!done && ((command = reader.readLine()) != null)) {
      LOG.log(Level.INFO, "COMMAND: {0}", command);

      //process v2
      done = processV2(command, reader, writer);

      writer.flush();
    }
  }

    /**
     * @brief Handle commands of version 2
     * @param command received by the server
     */
  protected boolean processV2(String command, BufferedReader reader, PrintWriter writer) throws IOException
  {
    nbrCommands++;
    switch (command.toUpperCase()) {

      case RouletteV2Protocol.CMD_CLEAR:

        store.clear();
        writer.println(RouletteV2Protocol.RESPONSE_CLEAR_DONE);
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_LIST:


        // list of students in class StudentsList
        StudentsList slResponse = new StudentsList();
        slResponse.setStudents(store.listStudents());
        writer.println(JsonObjectMapper.toJson(slResponse));
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_RANDOM:

        RandomCommandResponse rcResponse = new RandomCommandResponse();
        try {
          rcResponse.setFullname(store.pickRandomStudent().getFullname());
        } catch (EmptyStoreException ex) {
          rcResponse.setError("There is no student, you cannot pick a random one");
        }
        writer.println(JsonObjectMapper.toJson(rcResponse));
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_HELP:

        writer.println("Commands: " + Arrays.toString(RouletteV2Protocol.SUPPORTED_COMMANDS));
        break;
      case RouletteV2Protocol.CMD_INFO:
        nbrCommands++;
        InfoCommandResponse response = new InfoCommandResponse(RouletteV2Protocol.VERSION, store.getNumberOfStudents());
        writer.println(JsonObjectMapper.toJson(response));
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_LOAD:

        writer.println(RouletteV2Protocol.RESPONSE_LOAD_START);
        writer.flush();

        int nbrStudends = store.getNumberOfStudents();
        store.importData(reader);
        int nbrNewStudents = store.getNumberOfStudents() - nbrStudends;

        // prepare the response
        LoadCommandResponse lcResponse = new LoadCommandResponse("success", nbrNewStudents);
        writer.println(JsonObjectMapper.toJson(lcResponse)); // send response
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_BYE:

        ByeCommandResponse bcResponse = new ByeCommandResponse("success", nbrCommands);
        writer.println(JsonObjectMapper.toJson(bcResponse));
        writer.flush();
        return true;
      default:
        --nbrCommands;
        writer.println("Huh? please use HELP if you don't know what commands are available.");
        writer.flush();
        break;
    }
    return false;
  }

}
