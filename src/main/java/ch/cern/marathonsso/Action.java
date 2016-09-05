package ch.cern.marathonsso;

import mesosphere.marathon.plugin.auth.AuthorizedAction;

/**
 * Enumeration for handling AuthorizedActions more easily in Java.
 */
public enum Action {

    CreateApp(mesosphere.marathon.plugin.auth.CreateApp$.MODULE$),
    UpdateApp(mesosphere.marathon.plugin.auth.UpdateApp$.MODULE$),
    DeleteApp(mesosphere.marathon.plugin.auth.DeleteApp$.MODULE$),
    ViewApp(mesosphere.marathon.plugin.auth.ViewApp$.MODULE$),
    CreateGroup(mesosphere.marathon.plugin.auth.CreateGroup$.MODULE$),
    UpdateGroup(mesosphere.marathon.plugin.auth.UpdateGroup$.MODULE$),
    DeleteGroup(mesosphere.marathon.plugin.auth.DeleteGroup$.MODULE$),
    ViewGroup(mesosphere.marathon.plugin.auth.ViewGroup$.MODULE$);

    public static Action byAction(AuthorizedAction<?> action) {
        for (Action a : values()) {
            if (a.action.equals(action)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown Action: " + action);
    }

    private final AuthorizedAction<?> action;
    Action(AuthorizedAction<?> action) {
        this.action = action;
    }
}
