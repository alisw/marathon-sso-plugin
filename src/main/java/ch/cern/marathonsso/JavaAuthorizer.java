package ch.cern.marathonsso;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import scala.collection.JavaConverters;

import org.apache.log4j.Logger;

import mesosphere.marathon.plugin.Group;
import mesosphere.marathon.plugin.AppDefinition;
import mesosphere.marathon.plugin.PathId;

import mesosphere.marathon.plugin.auth.AuthorizedAction;
import mesosphere.marathon.plugin.auth.Authorizer;
import mesosphere.marathon.plugin.auth.Identity;
import mesosphere.marathon.plugin.http.HttpRequest;
import mesosphere.marathon.plugin.http.HttpResponse;

public class JavaAuthorizer implements Authorizer {
    private final Logger log = Logger.getLogger("MarathonSSO");

    private final String ADMIN_GROUP = System.getenv("MARATHON_SSO_ADMIN_GROUP");
    private final String VALID_GROUP_PREFIX = System.getenv("MARATHON_SSO_VALID_GROUP_PREFIX");

    @Override
    public <Resource> boolean isAuthorized(Identity principal, AuthorizedAction<Resource> action, Resource resource) {
        if (!(principal instanceof JavaIdentity))
          return false;
        JavaIdentity jid = (JavaIdentity) principal;
        if (jid.getName() == null)
          return false;

        // The authorization only happens o a path basis.
        if (resource instanceof AppDefinition)
          return isAuthorized(jid, Action.byAction(action), ((AppDefinition) resource).id());
        if (resource instanceof Group)
        {
          Group group = (Group) resource;
          Action a = Action.byAction(action);
          return viewRootPath(a, group.id()) || isAuthorized(jid, a, group.id());
        }
        return false;
    }

    // Top level path can be seen by anyone
    private boolean viewRootPath(Action action, PathId path) {
      if (path.path().size() == 0 && action == Action.ViewGroup)
        return true;
      return false;
    }
    private boolean isAuthorized(JavaIdentity principal, Action action, PathId path) {
        try {
          List<String> egroups = Arrays.asList(principal.getEgroups())
                                 .stream()
                                 .filter(e -> e.startsWith(VALID_GROUP_PREFIX))
                                 .map(e -> e.replace(VALID_GROUP_PREFIX, ""))
                                 .collect(Collectors.toList());
          log.info("Valid egroups to be used for authorization: " + egroups.toString());

          // An admin can do anything.
          if (egroups.contains(ADMIN_GROUP))
          {
            log.info("You are an admin. Authorizing.");
            return true;
          }
          log.info("You are not an admin.");

          // Get The base group and the application name
          log.info("Requested path " + path.path().toString());

          scala.collection.Seq<String> realPath = path.path();
          String root = realPath.head();
          String basename = path.path().last();
          String username = principal.getName();

          // Only admins can touch /users
          if (realPath.size() == 1 && root.equals("users") && action.equals(Action.ViewGroup))
          {
            log.info("Viewing /users . Authorizing." + action.toString());
            return true;
          }
          if (realPath.size() == 1 && root.equals("users"))
          {
            log.info("Modifying /users. Denied. ");
            return false;
          }

          if (path.path().size() == 0)
          {
            log.info("Viewing /. Authorizing." + action.toString());
            return true;
          }
          // A <user> can modify /users/<user>/
          if (root.equals("users") && path.path().size() > 1)
          {
            String userPath = realPath.tail().head();
            if (username.equals(userPath))
            {
              log.info(username + " is owner of /users/" + userPath + "/ . Authorizing.");
              return true;
            }
            log.info(username + " is not owner of /users/" + userPath);
            return false;
          }
          log.info("You are not owner of /" + root + "/ .");

          // A member of <group> can modify /<group>/
          if (egroups.contains(root))
          {
            log.debug("User " + principal.getName() + " is in group " + root + ". Authorized.");
            return true;
          }
          log.debug("You are not memeber of " + root + ".");
        } catch (Exception ex) {
          log.error(ex);
        }
        log.info("No match found. Not authorized");
        return false;
    }

    @Override
    public void handleNotAuthorized(Identity principal, HttpResponse response) {
        response.status(403);
        response.body("application/json", "{\"problem\": \"Not Authorized to perform this action!\"}".getBytes());
    }
}
