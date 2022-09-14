package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.awt.RelativePoint;
import org.apache.commons.lang.text.StrBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFunction;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ExtMethodListPopup {

    static void show1(List<KtFunction> functions) {

        StrBuilder b = new StrBuilder();
        for (KtFunction fun : functions) {
            b.append(fun.getContainingFile().getName() + ": " + fun.getName() + ", \n");
        }

        Notification n = new Notification("kt_ext_indexer", b.toString(), NotificationType.INFORMATION);
        Notifications.Bus.notify(n);
    }

    static void show(List<KtFunction> functions, MouseEvent e) {

        final RelativePoint popupPosition = RelativePoint.fromScreen(e.getLocationOnScreen());
        ListPopupStepImp step = new ListPopupStepImp(functions);
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

    static class ListPopupStepImp implements ListPopupStep<KtFunction> {

        List<KtFunction> functions;
        OnSelectedListener<KtFunction> selectedListener;

        public ListPopupStepImp(List<KtFunction> functions) {
            this.functions = functions;
            this.functions.add(null);
        }

        @Override
        public @NotNull List<KtFunction> getValues() {
            return functions;
        }

        @Override
        public boolean isSelectable(KtFunction value) {
            return value != null;
        }

        @Override
        public @Nullable Icon getIconFor(KtFunction value) {
            if (value == null) {
                return null;
            }
            return ShowExtIcon.icon;
        }

        @Override
        public @NlsContexts.ListItem @NotNull String getTextFor(KtFunction value) {
            if (value == null) {
                return "Non-Project";
            }
            return value.getContainingKtFile().getName() + "  " + value.getName();
        }

        @Override
        public @Nullable ListSeparator getSeparatorAbove(KtFunction value) {
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
        public @Nullable PopupStep<?> onChosen(KtFunction selectedValue, boolean finalChoice) {
            if (selectedValue == null) {
                return null;
            }
            selectedListener.onSelected(selectedValue);
            return FINAL_CHOICE;
        }

        @Override
        public boolean hasSubstep(KtFunction selectedValue) {
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
        public @Nullable MnemonicNavigationFilter<KtFunction> getMnemonicNavigationFilter() {
            return null;
        }

        @Override
        public boolean isSpeedSearchEnabled() {
            return false;
        }

        @Override
        public @Nullable SpeedSearchFilter<KtFunction> getSpeedSearchFilter() {
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
