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
    switch (command.toUpperCase()) {
      case RouletteV2Protocol.CMD_CLEAR:
        nbrCommands++;
        store.clear();
        writer.println(RouletteV2Protocol.RESPONSE_CLEAR_DONE);
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_LIST:
        nbrCommands++;

        // list of students in class StudentsList
        StudentsList slResponse = new StudentsList();
        slResponse.setStudents(store.listStudents());
        writer.println(JsonObjectMapper.toJson(slResponse));
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_RANDOM:
        nbrCommands++;
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
        nbrCommands++;
        writer.println("Commands: " + Arrays.toString(RouletteV2Protocol.SUPPORTED_COMMANDS));
        break;
      case RouletteV2Protocol.CMD_INFO:
        nbrCommands++;
        InfoCommandResponse response = new InfoCommandResponse(RouletteV2Protocol.VERSION, store.getNumberOfStudents());
        writer.println(JsonObjectMapper.toJson(response));
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_LOAD:
        nbrCommands++;
        writer.println(RouletteV2Protocol.RESPONSE_LOAD_START);
        writer.flush();

        List<Student> studentsToAdd;
        studentsToAdd = importStudents(reader); // temporise students to add
        int nbrStudends = studentsToAdd.size();

        synchronized (this)
        {
          // add the students to the store
          for(Student s : studentsToAdd) {
            store.addStudent(s);
          }
        }
        // prepare the response
        LoadCommandResponse lcResponse = new LoadCommandResponse("success", nbrStudends);
        writer.println(JsonObjectMapper.toJson(lcResponse)); // send response
        writer.flush();
        break;
      case RouletteV2Protocol.CMD_BYE:
        nbrCommands++;
        ByeCommandResponse bcResponse = new ByeCommandResponse("success", nbrCommands);
        writer.println(JsonObjectMapper.toJson(bcResponse));
        writer.flush();
        return true;
      default:
        writer.println("Huh? please use HELP if you don't know what commands are available.");
        writer.flush();
        break;
    }
    return false;
  }

  /**
   * @brief return a list of students to add
   * @return
   */
  private List<Student> importStudents(BufferedReader reader) throws IOException {
    List<Student> studentsToAdd = new ArrayList<>();
    String record;
    boolean endReached = false;
    while (!endReached && (record = reader.readLine()) != null)
    {
      if (record.equalsIgnoreCase(RouletteV2Protocol.CMD_LOAD_ENDOFDATA_MARKER))
      {
        LOG.log(Level.INFO, "End of stream reached. New students have been added to the store.");
        endReached = true;
      }
      else
      {
        LOG.log(Level.INFO, "Adding student {0} to the store.", record);
        studentsToAdd.add(new Student(record));
      }
    }
    return studentsToAdd;
  }

}
