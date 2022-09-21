package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtCallableDeclaration;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ExtMethodListPopup {
    static void show(List<KtCallableDeclaration> declarations, MouseEvent e) {
        final RelativePoint popupPosition = RelativePoint.fromScreen(e.getLocationOnScreen());
        ListPopupStepImp step = new ListPopupStepImp(declarations);
        step.selectedListener = ktFunction -> {
            if (ktFunction.canNavigate()) {
                ktFunction.navigate(true);
            }
        };
        ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);
        popup.show(popupPosition);
    }

    interface OnSelectedListener<T> {
        void onSelected(T t);
    }

    static class ListPopupStepImp implements ListPopupStep<KtCallableDeclaration> {

        final List<KtCallableDeclaration> declarations;
        OnSelectedListener<KtCallableDeclaration> selectedListener;

        public ListPopupStepImp(List<KtCallableDeclaration> declarations) {
            this.declarations = declarations;
        }

        @Override
        public @NotNull List<KtCallableDeclaration> getValues() {
            return declarations;
        }

        @Override
        public boolean isSelectable(KtCallableDeclaration value) {
            return value != null;
        }

        @Override
        public @Nullable Icon getIconFor(KtCallableDeclaration value) {
            if (value == null) {
                return null;
            }
            return ShowExtIcon.icon;
        }

        @Override
        public @NlsContexts.ListItem @NotNull String getTextFor(KtCallableDeclaration value) {
            if (value == null) {
                return "Non-Project";
            }
            return value.getContainingKtFile().getName() + "  " + value.getName();
        }

        @Override
        public @Nullable ListSeparator getSeparatorAbove(KtCallableDeclaration value) {
            return null;
        }

        @Override
        public int getDefaultOptionIndex() {
            return 0;
        }

        @Override
        public @NlsContexts.PopupTitle @Nullable String getTitle() {
            return "Extension Methods";
        }

        @Override
        public @Nullable PopupStep<?> onChosen(KtCallableDeclaration selectedValue, boolean finalChoice) {
            if (selectedValue == null) {
                return null;
            }
            selectedListener.onSelected(selectedValue);
            return FINAL_CHOICE;
        }

        @Override
        public boolean hasSubstep(KtCallableDeclaration selectedValue) {
            return selectedValue == null;
        }

        @Override
        public void canceled() {

        }

        @Override
        public boolean isMnemonicsNavigationEnabled() {
            return false;
        }

        @Override
        public @Nullable MnemonicNavigationFilter<KtCallableDeclaration> getMnemonicNavigationFilter() {
            return null;
        }

        @Override
        public boolean isSpeedSearchEnabled() {
            return false;
        }

        @Override
        public @Nullable SpeedSearchFilter<KtCallableDeclaration> getSpeedSearchFilter() {
            return null;
        }

        @Override
        public boolean isAutoSelectionEnabled() {
            return false;
        }

        @Override
        public @Nullable Runnable getFinalRunnable() {
            return null;
        }
    }
}
