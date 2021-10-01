package com.eziosoft.DankWrapper.lib;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class PatchCode {

    // genaric vanilla directory & imageio patch
    public static byte[] PatchDirImageIO(Class<?> input, String name) throws IOException {
        ClassNode node = new ClassNode();
        ClassReader read = new ClassReader(input.getClassLoader().getResourceAsStream(name.replace(".", "/").concat(".class")));
        read.accept(node, ClassReader.EXPAND_FRAMES);
        // find main method
        MethodNode main = null;
        for (MethodNode mn : node.methods){
            if ("main".equals(mn.name)){
                main = mn;
                break;
            }
        } if (main == null){
            System.err.println("No main method found for patch class!");
            System.exit(-2);
        }
        // we also need the workdir thing to patch that
        FieldNode workDirNode = null;
        for (final FieldNode fieldNode : node.fields) {
            final String fileTypeDescriptor = Type.getDescriptor(File.class);
            if (fileTypeDescriptor.equals(fieldNode.desc) && (fieldNode.access & ACC_STATIC) == ACC_STATIC) {
                workDirNode = fieldNode;
                break;
            }
        }
        // we can prepare to inject our patch now
        MethodNode patch = new MethodNode();
        Label patchlabel = new Label();
        patch.visitLabel(patchlabel);
        patch.visitLineNumber(99999999, patchlabel);
        patch.visitMethodInsn(INVOKESTATIC, "com/eziosoft/DankWrapper/Patches", "workDirPatch", "()Ljava/io/File;");
        patch.visitFieldInsn(PUTSTATIC, "net/minecraft/client/Minecraft", workDirNode.name, "Ljava/io/File;");
        // insert the patch
        main.instructions.insert(patch.instructions);
        DankClassWriter write = new DankClassWriter(DankClassWriter.COMPUTE_MAXS | DankClassWriter.COMPUTE_FRAMES);
        node.accept(write);
        return write.toByteArray();
    }
}
