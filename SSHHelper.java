import com.jcraft.jsch.*;
import java.util.Properties;
import java.io.Console;
import java.util.Scanner;

public class SSHHelper {

  static {
    JSch.setLogger(new Logger() {
      public boolean isEnabled(int level){ return true; }
      public void log(int level, String msg){ System.out.println("JSch["+level+"]: "+msg); }
    });
  }

  // Gestisce i prompt keyboard-interactive (Password:, OTP:, ecc.)
  static class InteractiveUserInfo implements UserInfo, UIKeyboardInteractive {
    private String cachedPassword;

    public String getPassword() { return cachedPassword; }
    public boolean promptYesNo(String s) { System.out.println(s+" [y/N]"); return readLine().trim().equalsIgnoreCase("y"); }
    public String getPassphrase() { return null; }
    public boolean promptPassphrase(String message) { return false; }
    public boolean promptPassword(String message) { // usato raramente in KI
      System.out.print(message + " ");
      cachedPassword = readSecret();
      return true;
    }
    public void showMessage(String message) { System.out.println(message); }

    public String[] promptKeyboardInteractive(String dest, String name, String instruction,
                                             String[] prompts, boolean[] echos) {
      if (instruction != null && !instruction.isEmpty()) System.out.println(instruction);
      String[] answers = new String[prompts.length];
      for (int i = 0; i < prompts.length; i++) {
        System.out.print(prompts[i] + " ");
        if (echos != null && echos.length > i && !echos[i]) {
          answers[i] = readSecret();                 // Password non in chiaro
          if (prompts[i].toLowerCase().contains("password"))
            cachedPassword = answers[i];
        } else {
          answers[i] = readLine();
        }
      }
      return answers;
    }

    private static String readLine() {
      Console c = System.console();
      if (c != null) return c.readLine();
      return new Scanner(System.in).nextLine();
    }
    private static String readSecret() {
      Console c = System.console();
      if (c != null) return new String(c.readPassword());
      return new Scanner(System.in).nextLine(); // in IDE non si può mascherare
    }
  }

  public static void main(String[] args) {
    String host = "psmp-lsgd-p01"; // o quello giusto
    int    port = 22;
    String user = "DOMINIO\\utente"; // o utente@dominio se richiesto

    try {
      JSch jsch = new JSch();
      Session session = jsch.getSession(user, host, port);

      Properties cfg = new Properties();
      cfg.put("StrictHostKeyChecking", "no");
      cfg.put("PreferredAuthentications", "keyboard-interactive,password"); // forza KI
      cfg.put("userauth.gssapi-with-mic", "no");
      session.setConfig(cfg);

      session.setUserInfo(new InteractiveUserInfo()); // qui avverrà il prompt "Password:"

      System.out.println("Connecting...");
      session.connect(20000);
      System.out.println("✅ Autenticato e connesso");

      session.disconnect();
    } catch (JSchException e) {
      System.err.println("❌ SSH failed: " + e.getMessage());
    }
  }
}
