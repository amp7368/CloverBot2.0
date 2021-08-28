package apple.inactivity.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class InactivityListener {
    public final String type;

    public InactivityListener(String type) {
        this.type = type;
    }

    public abstract String prettyString();

    public abstract void trigger(int daysInactiveToTrigger, String player);

    public enum InactivityListenerType {
        PING("ping", "Ping/Log", InactivityListenerPing.class, InactivityListenerPing::new);

        private static Map<String, InactivityListenerType> listeners = null;
        private final String typeName;
        private final String prettyName;
        private final Class<? extends InactivityListener> typeClass;
        private Supplier<InactivityListener> constructor;

        InactivityListenerType(String typeName, String prettyName, Class<? extends InactivityListener> typeClass, Supplier<InactivityListener> constructor) {
            this.typeName = typeName;
            this.prettyName = prettyName;
            this.typeClass = typeClass;
            this.constructor = constructor;
        }

        public static InactivityListenerType from(String id) {
            if (listeners == null) {
                listeners = new HashMap<>();
                for (InactivityListenerType type : values()) {
                    listeners.put(type.getTypeId(), type);
                }
            }
            return listeners.getOrDefault(id, PING);
        }

        public String getTypeId() {
            return typeName;
        }

        public String getPrettyName() {
            return prettyName;
        }

        public Class<? extends InactivityListener> getTypeClass() {
            return typeClass;
        }

        public InactivityListener getCreator() {
            return constructor.get();
        }
    }
}
