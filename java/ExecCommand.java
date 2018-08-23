
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class ExecCommand {
  private class ErrorReader extends Thread {
    public ErrorReader() {
      try {
        ExecCommand.this.errorSem = new Semaphore(1);
        ExecCommand.this.errorSem.acquire();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        final StringBuffer readBuffer = new StringBuffer();
        final BufferedReader isr = new BufferedReader(
            new InputStreamReader(ExecCommand.this.p.getErrorStream()));
        String buff = new String();
        while ((buff = isr.readLine()) != null) {
          readBuffer.append(buff);
        }
        ExecCommand.this.error = readBuffer.toString();
        ExecCommand.this.errorSem.release();
      } catch (final IOException e) {
        e.printStackTrace();
      }
      if (ExecCommand.this.error.length() > 0) {
        System.out.println(ExecCommand.this.error);
      }
    }
  }

  private class InputWriter extends Thread {
    private String input;

    public InputWriter(final String input) {
      this.input = input;
    }

    @Override
    public void run() {
      final PrintWriter pw = new PrintWriter(ExecCommand.this.p.getOutputStream());
      pw.println(this.input);
      pw.flush();
    }
  }

  private class OutputReader extends Thread {
    public OutputReader() {
      try {
        ExecCommand.this.outputSem = new Semaphore(1);
        ExecCommand.this.outputSem.acquire();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        final StringBuffer readBuffer = new StringBuffer();
        final BufferedReader isr = new BufferedReader(
            new InputStreamReader(ExecCommand.this.p.getInputStream()));
        String buff = new String();
        while ((buff = isr.readLine()) != null) {
          readBuffer.append(buff);
          System.out.println(buff);
        }
        ExecCommand.this.output = readBuffer.toString();
        ExecCommand.this.outputSem.release();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  private Semaphore outputSem;
  private String output;

  private Semaphore errorSem;

  private String error;

  private Process p;

  public ExecCommand(final String command) {

    System.out.println("Run command: " + command);

    try {
      this.p = Runtime.getRuntime().exec(this.makeArray(command));
      new OutputReader().start();
      new ErrorReader().start();
      if (this.p.waitFor() != 0) {
        System.exit(1);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  public ExecCommand(final String command, final String input) {
    try {
      this.p = Runtime.getRuntime().exec(this.makeArray(command));
      new InputWriter(input).start();
      new OutputReader().start();
      new ErrorReader().start();
      this.p.waitFor();
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  public String getError() {
    try {
      this.errorSem.acquire();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    final String value = this.error;
    this.errorSem.release();
    return value;
  }

  public String getOutput() {
    try {
      this.outputSem.acquire();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    final String value = this.output;
    this.outputSem.release();
    return value;
  }

  private String[] makeArray(final String command) {
    final ArrayList<String> commandArray = new ArrayList<>();
    String buff = "";
    boolean lookForEnd = false;
    for (int i = 0; i < command.length(); i++) {
      if (lookForEnd) {
        if (command.charAt(i) == '\"') {
          if (buff.length() > 0) {
            commandArray.add(buff);
          }
          buff = "";
          lookForEnd = false;
        } else {
          buff += command.charAt(i);
        }
      } else {
        if (command.charAt(i) == '\"') {
          lookForEnd = true;
        } else if (command.charAt(i) == ' ') {
          if (buff.length() > 0) {
            commandArray.add(buff);
          }
          buff = "";
        } else {
          buff += command.charAt(i);
        }
      }
    }
    if (buff.length() > 0) {
      commandArray.add(buff);
    }

    final String[] array = new String[commandArray.size()];
    for (int i = 0; i < commandArray.size(); i++) {
      array[i] = commandArray.get(i);
    }

    return array;
  }
}
