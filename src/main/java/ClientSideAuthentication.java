import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

class ClientSideAuthentication {

    private String passwordHash;

    void setCredentials(char[] password){
        StringBuilder pwStringBuilder = new StringBuilder();
        for (char c : password){
            pwStringBuilder.append(c);
        }
        System.out.println(pwStringBuilder.toString());
        passwordHash = hash(pwStringBuilder.toString());
    }

    void clearCredentials(){
        passwordHash = null;
    }

    boolean passwordIsSet(){
        return passwordHash != null;
    }

    // hashes the username, a one-time value provided by the server, and the password
    String getAuthToken(String nonce){
        if (passwordIsSet()) {
            return hash(passwordHash + nonce);
        }
        return null;
    }

    private String hash(String input) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        byte[] digest = digestSHA3.digest(input.getBytes());
        return Hex.toHexString(digest);
    }
}
