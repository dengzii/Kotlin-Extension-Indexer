package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class ShowExtIcon {
    private static Icon load() {
        return IconLoader.getIcon("/icons/show_ext.svg", ShowExtIcon.class);
    }

    public static final Icon icon = load();
}
