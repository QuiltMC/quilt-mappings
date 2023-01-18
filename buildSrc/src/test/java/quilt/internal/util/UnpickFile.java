package quilt.internal.util;

import daomephsta.unpick.impl.representations.AbstractConstantGroup;
import daomephsta.unpick.impl.representations.FlagConstantGroup;
import daomephsta.unpick.impl.representations.SimpleConstantGroup;
import daomephsta.unpick.impl.representations.TargetMethods;

import java.util.Map;

public class UnpickFile {
    private final Map<String, AbstractConstantGroup<?>> groups;
    private final TargetMethods targetMethods;

    public UnpickFile(Map<String, AbstractConstantGroup<?>> groups, TargetMethods.Builder targetMethodsBuilder) {
        this(groups, targetMethodsBuilder.build());
    }

    public UnpickFile(Map<String, AbstractConstantGroup<?>> groups, TargetMethods targetMethods) {
        this.groups = groups;
        this.targetMethods = targetMethods;
    }

    public boolean containsGroup(String name) {
        return groups.containsKey(name);
    }

    public boolean isFlagGroup(String name) {
        return groups.containsKey(name) && groups.get(name) instanceof FlagConstantGroup;
    }

    public boolean isSimpleConstantGroup(String name) {
        return groups.containsKey(name) && groups.get(name) instanceof SimpleConstantGroup;
    }

    public String getParameterConstantGroup(String methodOwner, String methodName, String methodDescriptor, int parameterIndex) {
        return targetMethods.getParameterConstantGroup(methodOwner, methodName, methodDescriptor, parameterIndex);
    }

    public String getReturnConstantGroup(String methodOwner, String methodName, String methodDescriptor) {
        return targetMethods.getReturnConstantGroup(methodOwner, methodName, methodDescriptor);
    }
}
