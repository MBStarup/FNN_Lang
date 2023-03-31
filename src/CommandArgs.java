import java.util.*;

public class CommandArgs {
    public Map<String, List<String>> Flags;
    public List<String> Rest;

    String[][] commands = {
            { "-no-remove-c", },
            { "-o", "path" },
    };

    public CommandArgs(String[] argv) {
        Flags = new HashMap<>();
        Rest = new Vector<>();
        for (String[] command : commands) {
            Flags.put(command[0], Arrays.asList(command).subList(1, command.length));
        }

        for (int i = 0; i < argv.length; i++) {
            if (Flags.containsKey(argv[i])) {

            }
        }

    }

}
