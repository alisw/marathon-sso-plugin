package ch.cern.marathonsso;

import mesosphere.marathon.plugin.auth.Identity;
import java.util.Vector;

class JavaIdentity implements Identity {

    private final String name;
    private final String[] egroups;

    public JavaIdentity(String name, String[] egroups) {
        this.name = name;
        this.egroups = egroups;
    }

    public String getName() {
        return name;
    }

    public String[] getEgroups() {
        return egroups;
    }
}
