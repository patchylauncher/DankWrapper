package com.eziosoft.DankWrapper.injectors;

import com.eziosoft.DankWrapper.lib.DankClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.imageio.ImageIO;
import java.io.File;

import static org.objectweb.asm.Opcodes.*;

public class MojangVanillaFix extends BasicInjector{

    /**
     * This is essentially what the official Mojang Legacy Launcher does for older versions of minecraft.
     * It sets the game directory to whatever it needs to be, based on provided launch arguments
     */

    public MojangVanillaFix(){
        this.targetclass = "net.minecraft.client.Minecraft";
        this.launchtarget = "net.minecraft.client.Minecraft";
        this.hasOptions = true;
        this.shortarg = "d";
        this.desc = "Game directory";
        this.required = true;
        this.hasLaunchTarget = true;
        this.acceptsParams = true;
    }

    private static String workdir;

    @Override
    public byte[] inject(byte[] input) throws Exception {
        ClassNode node = new ClassNode();
        ClassReader read = new ClassReader(input);
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
        patch.visitLineNumber(420, patchlabel);
        patch.visitMethodInsn(INVOKESTATIC, "com/eziosoft/DankWrapper/injectors/MojangVanillaFix", "VanillaPatch", "()Ljava/io/File;");
        patch.visitFieldInsn(PUTSTATIC, "net/minecraft/client/Minecraft", workDirNode.name, "Ljava/io/File;");
        // insert the patch
        main.instructions.insert(patch.instructions);
        DankClassWriter write = new DankClassWriter(DankClassWriter.COMPUTE_MAXS | DankClassWriter.COMPUTE_FRAMES);
        node.accept(write);
        return write.toByteArray();
    }

    @Override
    public void AcceptArgument(String input) {
        workdir = input;
    }

    public static File VanillaPatch(){
        // nuke disk caching
        System.out.println("Turning off imageio disk caching...");
        ImageIO.setUseCache(false);
        // todo: shit
        System.out.println("Setting gamedir to " + workdir);
        return new File(workdir);

    }
}
