import com.jcraft.jsch.*;

class KIUserInfo implements UserInfo, UIKeyboardInteractive {
    private final String password;
    private final String otp; // opzionale

    KIUserInfo(String password, String otp) {
        this.password = password;
        this.otp = otp;
    }
    public String getPassword() { return password; }
    public boolean promptYesNo(String s) { return true; }
    public String getPassphrase() { return null; }
    public boolean promptPassphrase(String message) { return false; }
    public boolean promptPassword(String message) { return true; }
    public void showMessage(String message) { System.out.println(message); }

    // Rispondi ai prompt keyboard-interactive
    public String[] promptKeyboardInteractive(String dest, String name, String instruction,
                                             String[] prompts, boolean[] echos) {
        String[] answers = new String[prompts.length];
        for (int i = 0; i < prompts.length; i++) {
            String p = prompts[i].toLowerCase();
            if (p.contains("password"))       answers[i] = password;
            else if (p.contains("otp") || p.contains("token") || p.contains("second")) answers[i] = otp;
            else answers[i] = ""; // oppure leggi da console se serve
        }
        return answers;
    }
}

// Uso:
JSch jsch = new JSch();
Session session = jsch.getSession("utente", "host", 22);
session.setUserInfo(new KIUserInfo("laTuaPassword", "123456")); // metti OTP se richiesto

Properties cfg = new Properties();
cfg.put("StrictHostKeyChecking", "no");
cfg.put("PreferredAuthentications", "keyboard-interactive,password");
session.setConfig(cfg);

session.connect(20000);
System.out.println("✅ SSH connesso");
