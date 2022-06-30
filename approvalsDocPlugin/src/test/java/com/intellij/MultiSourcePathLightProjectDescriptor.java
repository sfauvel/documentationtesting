package com.intellij;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MultiSourcePathLightProjectDescriptor extends LightProjectDescriptor {

    private List<Path> sourcePaths;

    public <T> MultiSourcePathLightProjectDescriptor(List<Path> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setUpProject(@NotNull Project project, @NotNull LightProjectDescriptor.SetupHandler handler) throws Exception {
        WriteAction.run(() -> {
            Module module = createMainModule(project);
            handler.moduleCreated(module);
            createSourceRoots(handler, module, sourcePaths);
        });
    }

    private void createSourceRoots(@NotNull SetupHandler handler, Module module, List<Path> paths) {
        for (Path srcPath : paths) {
            VirtualFile sourceRoot = createSourceRoot(module, srcPath);
            if (sourceRoot != null) {
                handler.sourceRootCreated(sourceRoot);
                createContentEntry(module, sourceRoot);
            }
        }
    }

    protected VirtualFile createSourceRoot(@NotNull Module module, Path srcPath) {
        VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
        assert dummyRoot != null;
        dummyRoot.refresh(false, false);

        VirtualFile srcRoot = dummyRoot;
        for (Path path : srcPath) {
            srcRoot = doCreateSourceRoot(srcRoot, path.toString());
        }
        registerSourceRoot(module.getProject(), srcRoot);
        return srcRoot;
    }

    protected VirtualFile doCreateSourceRoot(VirtualFile root, String srcPath) {
        VirtualFile srcRoot;
        try {
            srcRoot = root.createChildDirectory(this, srcPath);
            //cleanSourceRoot(srcRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return srcRoot;
    }
}
