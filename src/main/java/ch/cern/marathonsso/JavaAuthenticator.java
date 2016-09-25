package ch.cern.marathonsso;

import java.util.Base64;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;



import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import mesosphere.marathon.plugin.auth.Authenticator;
import mesosphere.marathon.plugin.auth.Identity;
import mesosphere.marathon.plugin.http.HttpRequest;
import mesosphere.marathon.plugin.http.HttpResponse;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class JavaAuthenticator implements Authenticator {
    private final Logger log = Logger.getLogger("MarathonSSO");

    private final ExecutionContext EC = ExecutionContexts.fromExecutorService(Executors.newSingleThreadExecutor());
    private final String LOGIN_HEADER = System.getenv("MARATHON_SSO_LOGIN_HEADER");
    private final String GROUP_HEADER = System.getenv("MARATHON_SSO_GROUP_HEADER");
    public JavaAuthenticator() {
      log.info("Constructed");
    }

    @Override
    public Future<Option<Identity>> authenticate(HttpRequest request) {
      return Futures.future(() -> Option.apply(doAuth(request)), EC);
    }

    private Identity doAuth(HttpRequest request) {
      try {
        Option<String> header = request.header(LOGIN_HEADER).headOption();
        Option<String> groupHeader = request.header(GROUP_HEADER).headOption();

        // In case we do not have neither the login header nor the
        // group header it means marathon is being accessed directly
        // from within the cluster, e.g. by traefik, so we always allow
        // it given in any case one could spoof the connection.
        if (!header.isDefined() && ! groupHeader.isDefined())
        {
          log.debug("Direct access to backend.");
          return new JavaIdentity(null, null);
        }
        String user = null;
        if (header.isDefined()) {
          user = header.get();
        }
        String[] egroups = {};
        if (groupHeader.isDefined()) {
          egroups = groupHeader.get().split(";");
        }
        JavaIdentity identity = new JavaIdentity(user, egroups);
        log.debug("Username: " + identity.getName() + ", groups: " + Arrays.toString(identity.getEgroups()));
        return identity;
      } catch (Exception ex) {
        log.error(ex);
      }
      return null;
    }

    @Override
    public void handleNotAuthenticated(HttpRequest request, HttpResponse response) {
      response.status(401);
      response.body("application/json", "{\"problem\": \"Not Authenticated!\"}".getBytes());
    }
}
