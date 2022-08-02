package ti.gateway.kubernetes.tls;

import java.util.List;

class TlsServer {
    private TlsSecret secret;
    private List<String> hosts;

    TlsServer() {
    }

    public TlsSecret getSecret() {
        return this.secret;
    }

    public TlsServer setSecret(TlsSecret certs) {
        this.secret = certs;
        return this;
    }

    public List<String> getHosts() {
        return this.hosts;
    }

    public TlsServer setHosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public String toString() {
        return "TlsServer{secret=" + this.secret + ", hosts=" + this.hosts + "}";
    }
}
