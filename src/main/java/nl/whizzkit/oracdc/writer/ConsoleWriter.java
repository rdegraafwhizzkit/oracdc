package nl.whizzkit.oracdc.writer;

public class ConsoleWriter implements IWritable {
    @Override
    public void write(String output) {
        System.out.println(output);
    }
}
