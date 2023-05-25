import java.util.*;

public class CommandArgs {
    public Map<String, Boolean> Flags = new HashMap<>();
    public Map<String, String> FlagDescriptions = new HashMap<>();
    public Map<String, String> Props = new HashMap<>();
    public Map<String, String> PropDescriptions = new HashMap<>();

    public List<String> Parse(String[] argv) {
        List<String> Remainder = new Vector<>();
        for (int i = 0; i < argv.length; i++) {
            if (Flags.keySet().contains(argv[i])) {
                Flags.put(argv[i], true);
            } else if (Props.keySet().contains(argv[i])) {
                Props.put(argv[i], argv[++i]);
            } else {
                Remainder.add(argv[i]);
                // Utils.ERREXIT("Unknown command argument: " + argv[i] + "\nUsage: " + this.toString());
            }
        }
        return Remainder;
    }

    public void AddFlag(String flag, String description) {
        Flags.put(flag, false);
        FlagDescriptions.put(flag, description);
    }

    public void AddProp(String prop, String defaultVal, String description) {
        Props.put(prop, defaultVal);
        PropDescriptions.put(prop, description);
    }

    public boolean IsSet(String flag) {
        var val = Flags.get(flag);
        return val != null && val;
    }

    public String GetProp(String prop) {
        return Props.get(prop);
    }

    @Override public String toString() {
        var result = "Flags:\n";
        for (var flag : Flags.keySet()) {
            result += "\t" + flag + ": " + FlagDescriptions.get(flag) + "\n";
        }
        result += "Properties:\n";
        for (var prop : Props.keySet()) {
            result += "\t" + prop + ": " + PropDescriptions.get(prop) + "\n";
        }
        return result;
    }

    public String State() {
        var result = "Flags:\n";
        for (var flag : Flags.keySet()) {
            result += "\t" + flag + ": " + Flags.get(flag) + "\n";
        }
        result += "Properties:\n";
        for (var prop : Props.keySet()) {
            result += "\t" + prop + ": " + Props.get(prop) + "\n";
        }
        return result;
    }

}
