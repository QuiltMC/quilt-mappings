package quilt.internal.tasks.unpick.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;

public class OpenGlConstantUnpickGenerator extends DefaultMappingsTask implements UnpickGen {
    public static final String TASK_NAME = "openGlUnpickGen";
    public static final String OPEN_GL_REGISTRY = "https://raw.githubusercontent.com/KhronosGroup/OpenGL-Registry/main/xml/gl.xml";
    public static final String GL_STATE_MANAGER_CLASS = "com/mojang/blaze3d/platform/GlStateManager";

    public static final List<String> OPEN_GL_VERSIONS = List.of("11", "12", "13", "14", "15", "20", "21", "30", "31", "32");

    public OpenGlConstantUnpickGenerator() {
        super(Constants.Groups.UNPICK_GEN);
        this.dependsOn(DownloadMinecraftLibrariesTask.TASK_NAME);

        this.onlyIf(_task -> !_task.getProject().file("unpick-definitions/unpick_gl.unpick").exists() ||
                             !_task.getProject().file("unpick-definitions/unpick_glstatemanager.unpick").exists());
    }

    @TaskAction
    public void exec() throws Exception {
        JsonNode tree = new XmlMapper().readTree(new URL(OPEN_GL_REGISTRY));
        List<GlFunc> methods = new ArrayList<>();
        tree.get("commands").get("command").forEach(command -> {
            String name = command.get("proto").get("name").textValue();
            List<GlParam> params = new ArrayList<>();
            if (command.has("param")) {
                if (command.get("param").isArray()) {
                    command.get("param").forEach(param -> addParam(param, params));
                } else {
                    JsonNode param = command.get("param");
                    addParam(param, params);
                }
            }
            methods.add(new GlFunc(name, params));
        });

        Map<String, List<String>> groupToConstants = new HashMap<>();
        Map<String, Boolean> isBitmask = new HashMap<>();
        tree.get("enums").forEach(enumGroup -> {
            boolean bitmask = enumGroup.has("type") && enumGroup.get("type").asText().equals("bitmask");
            if (enumGroup.has("enum") && enumGroup.get("enum").isArray()) {
                enumGroup.get("enum").forEach(enumEntry -> {
                    if (!enumEntry.has("name") || !enumEntry.has("group")) {
                        return;
                    }

                    String name = enumEntry.get("name").asText();
                    String groups = enumEntry.get("group").asText();

                    for (String group : groups.split(",")) {
                        groupToConstants.computeIfAbsent(group, _group -> new ArrayList<>()).add(name);
                        isBitmask.putIfAbsent(group, bitmask);
                    }
                });
            }
        });

        Map<String, List<String>> constantToDefiningVersions = new HashMap<>();
        Map<String, Map<Signature, List<String>>> functionToSignatureToDefiningVersions = new HashMap<>();
        ZipFile zip = new ZipFile(getProject().file(".gradle/minecraft/libraries/lwjgl-opengl-3.3.1.jar"));
        OPEN_GL_VERSIONS.forEach(version -> {
            try {
                ZipEntry e = zip.getEntry("org/lwjgl/opengl/GL" + version + ".class");
                ClassReader reader = new ClassReader(zip.getInputStream(e).readAllBytes());

                ZipEntry e2 = zip.getEntry("org/lwjgl/opengl/GL" + version + "C.class");
                ClassReader reader2 = new ClassReader(zip.getInputStream(e2).readAllBytes());


                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                        constantToDefiningVersions.computeIfAbsent(name, n -> new ArrayList<>()).add(version);
                        return super.visitField(access, name, descriptor, signature, value);
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        functionToSignatureToDefiningVersions.computeIfAbsent(name, n -> new HashMap<>()).computeIfAbsent(Signature.from(descriptor), d -> new ArrayList<>()).add(version);
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                }, 1);

                reader2.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                        List<String> versions = constantToDefiningVersions.computeIfAbsent(name, n -> new ArrayList<>());
                        // We only want to use one version of the constants
                        if (!versions.contains(version)) {
                            versions.add(version + "C");
                        }
                        return super.visitField(access, name, descriptor, signature, value);
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        // We want to unpick all the methods
                        functionToSignatureToDefiningVersions.computeIfAbsent(name, n -> new HashMap<>()).computeIfAbsent(Signature.from(descriptor), d -> new ArrayList<>()).add(version + "C");
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                }, 1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        {
            File unpickGl = getProject().file("unpick-definitions/unpick_gl.unpick");
            unpickGl.delete();
            unpickGl.createNewFile();

            PrintStream out = new PrintStream(new FileOutputStream(unpickGl));
            out.println("v2\n# This file was automatically generated, do not modify it\n");

            for (String group : groupToConstants.keySet()) {
                boolean generated = false;

                String type = isBitmask.get(group) ? "flag" : "constant";

                for (String constant : groupToConstants.get(group)) {
                    if (!generated && constantToDefiningVersions.containsKey(constant)) {
                        generated = true;
                    }

                    constantToDefiningVersions
                            .getOrDefault(constant, Collections.emptyList())
                            .forEach(version -> out.println(type + " " + group + " org/lwjgl/opengl/GL" + version + " " + constant));
                }

                if (generated) {
                    out.println();
                }
            }

            for (GlFunc func : methods) {
                boolean shouldGenerate = func.parameters.stream().anyMatch(param -> isBitmask.containsKey(param.group));
                if (shouldGenerate) {
                    functionToSignatureToDefiningVersions.getOrDefault(func.name, new HashMap<>())
                            .forEach((signature, versions) -> {
                                versions.forEach(version -> {
                                    out.println("target_method org/lwjgl/opengl/GL" + version + " " + func.name + " " + signature.signature);
                                    printParams(isBitmask, out, func, false, signature);
                                });
                            });
                    functionToSignatureToDefiningVersions.getOrDefault("n" + func.name, new HashMap<>())
                            .forEach((signature, versions) -> {
                                versions.forEach(version -> {
                                    out.println("target_method org/lwjgl/opengl/GL" + version + " n" + func.name + " " + signature.signature);
                                    printParams(isBitmask, out, func, true, signature);
                                });
                            });
                    if (functionToSignatureToDefiningVersions.containsKey(func.name)) {
                        out.println();
                    }
                }
            }
        }

        {
            File unpickGlStateManager = getProject().file("unpick-definitions/unpick_glstatemanager.unpick");
            unpickGlStateManager.delete();
            unpickGlStateManager.createNewFile();

            PrintStream out = new PrintStream(new FileOutputStream(unpickGlStateManager));
            out.println("v2\n# This file was automatically generated, do not modify it\n");

            Map<String, List<Signature>> methodToSignature = new HashMap<>();
            ZipFile minecraftJar = new ZipFile(getProject().file(Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar"));
            ClassReader reader = new ClassReader(minecraftJar.getInputStream(minecraftJar.getEntry(GL_STATE_MANAGER_CLASS + ".class")).readAllBytes());

            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    methodToSignature.computeIfAbsent(name, n -> new ArrayList<>()).add(Signature.from(descriptor));
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }, 1);

            methodToSignature.forEach((method, signatures) -> {
                String strippedName = method.startsWith("_") ? method.substring(1) : method;
                String glName = strippedName.startsWith("gl") ? strippedName : "gl" + Character.toUpperCase(strippedName.charAt(0)) + strippedName.substring(1);

                List<GlFunc> funcs = methods.stream().filter(f -> f.name.equals(glName)).toList();
                if (funcs.size() > 1) {
                    throw new RuntimeException("Multiple methods found for " + method + ": " + funcs);
                }

                if (funcs.size() == 0) {
                    funcs = methods.stream().filter(f -> f.name.equals(glName + "i")).toList();
                    if (funcs.size() == 0) {
                        return;
                    }
                }

                GlFunc func = funcs.get(0);
                boolean shouldGenerate = func.parameters.stream().anyMatch(param -> isBitmask.containsKey(param.group));
                if (shouldGenerate) {
                    signatures.forEach(signature -> {
                        out.println("target_method " + GL_STATE_MANAGER_CLASS + " " + method + " " + signature.signature);
                        printParams(isBitmask, out, func, false, signature);
                    });
                    out.println();
                }
            });
        }
    }

    private void printParams(Map<String, Boolean> isBitmask, PrintStream out, GlFunc func, boolean isUnsafe, Signature signature) {
        for (int i = 0; i < func.parameters.size(); i++) {
            GlParam param = func.parameters.get(i);
            if (isBitmask.containsKey(param.group)) {
                if (!isUnsafe) {
                    if (func.name.equals("glBufferData") && i == 3) {
                        out.println("\tparam " + 2 + " " + param.group);
                        continue;
                    }
                }
                if (signature.isInt(i)) {
                    out.println("\tparam " + i + " " + param.group);
                }
            }
        }
    }

    private static void addParam(JsonNode param, List<GlParam> params) {
        if (!(param instanceof TextNode)) {
            params.add(new GlParam(param.get("name").asText(),
                    param.get("ptype") != null ? param.get("ptype").asText() : "",
                    param.get("group") != null ? param.get("group").asText() : ""));
        } else {
            params.add(new GlParam(param.asText(), "", ""));
        }
    }

    private record GlFunc(String name, List<GlParam> parameters) {
        @Override
        public String toString() {
            return name + ":\n" + String.join("\n", parameters.stream().map(Object::toString).toList()).indent(4);
        }
    }

    private record GlParam(String name, String ptype, String group) {
        @Override
        public String toString() {
            return String.format("%s %s", ptype, name) + (group.isEmpty() ? "" : "(" + group + ")");
        }
    }

    private record Signature(String signature, List<String> types) {
        private static Signature from(String signature) {
            String trimmed = signature.substring(1, signature.length() - 2);
            Matcher matcher = Pattern.compile("(L.+?;|.)").matcher(trimmed);
            matcher.find();
            return new Signature(signature, matcher.results().map(matchResult -> matchResult.group(1)).toList());
        }

        private boolean isInt(int index) {
            if (index >= types.size()) {
                return false;
            }
            return types.get(index).equals("I");
        }
    }
}
