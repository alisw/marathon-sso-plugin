package ch.cern.marathonsso;

import mesosphere.marathon.plugin.auth.Identity;
import java.util.Vector;

class JavaIdentity implements Identity {

    private final String name;
    private final String[] egroups;
    private final boolean isDirect;

    public JavaIdentity(String name, String[] egroups) {
        this.name = name;
        this.egroups = egroups;
        if (name == null && egroups == null)
          this.isDirect = true;
        else
          this.isDirect = false;
    }

    public String getName() {
        return name;
    }

    public String[] getEgroups() {
        return egroups;
    }

    public boolean isDirect() {
      return this.isDirect;
    }
}
