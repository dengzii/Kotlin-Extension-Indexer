package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.Language;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.apache.commons.lang.text.StrBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtNamedFunction;

public class LineMarkerProviderImpl implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!element.getLanguage().is(Language.findLanguageByID("kotlin"))) {
            return null;
        }
        if (!(element instanceof KtClass)) {
            return null;
        }
        KtClass ktClass = (KtClass) element;

        GlobalSearchScope allScope = ProjectScope.getProjectScope(element.getProject());
        Query<PsiReference> search = ReferencesSearch.search(ktClass, allScope);
        for (PsiReference r : search) {
            KtFunction fun = getKtFunFromPsiReference(r);
            if (fun != null) {
                PsiElement nameIdentifier = ktClass.getNameIdentifier();
                return new LineMarkerInfo<>(nameIdentifier, nameIdentifier.getTextRange(), ShowExtIcon.icon, null, SHOW_RECEIVERS, GutterIconRenderer.Alignment.LEFT, () -> "--");
            }
        }
        return null;
    }

    private static KtFunction getKtFunFromPsiReference(PsiReference reference) {
        PsiElement element = reference.getElement();
        PsiElement funcEle = element;
        for (int i = 0; i < 3; i++) {
            if (element.getParent() == null) {
                return null;
            }
            funcEle = funcEle.getParent();
        }
        if (!(funcEle instanceof KtNamedFunction)) {
            return null;
        }
        return (KtNamedFunction) funcEle;
    }

    private static GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS = (e, psiElement) -> {
        KtClass ktClass = ((KtClass) psiElement.getParent());

        GlobalSearchScope allScope = ProjectScope.getProjectScope(ktClass.getProject());
        Query<PsiReference> search = ReferencesSearch.search(ktClass, allScope);
        StrBuilder b = new StrBuilder();
        for (PsiReference r : search) {
            KtFunction fun = getKtFunFromPsiReference(r);
            if (fun != null) {
                b.append(fun.getContainingFile().getName() + ":" + fun.getName() + " ## \n");
            }
        }

        Notification n = new Notification("Extension_Methods", b.toString(), NotificationType.INFORMATION);
        Notifications.Bus.notify(n);
    };
}
