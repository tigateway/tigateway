package ti.gateway.kubernetes.tls;

import java.io.File;

class TlsSecret {
    private final File certFile;
    private final File keyFile;

    TlsSecret(String secretPath) {
        this.certFile = new File(secretPath, "tls.crt");
        this.keyFile = new File(secretPath, "tls.key");
    }

    public File getCertFile() {
        return this.certFile;
    }

    public File getKeyFile() {
        return this.keyFile;
    }

    public String toString() {
        return "TlsCerts{certFile=" + this.certFile + ", keyFile=" + this.keyFile + "}";
    }
}
