package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtUserType;

import java.util.ArrayList;
import java.util.List;

public class LineMarkerProviderImpl implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof KtClass) && !(element instanceof PsiClass)) {
            return null;
        }

        GlobalSearchScope allScope = ProjectScope.getProjectScope(element.getProject());
        Query<PsiReference> search = ReferencesSearch.search(element, allScope);
        for (PsiReference r : search) {
            KtFunction fun = getKtFunFromPsiReference(r);
            if (fun != null) {
                PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
                if (nameIdentifier == null) {
                    continue;
                }
                return new LineMarkerInfo<>(nameIdentifier, nameIdentifier.getTextRange(), ShowExtIcon.icon, null, SHOW_RECEIVERS, GutterIconRenderer.Alignment.LEFT, () -> "extension methods");
            }
        }
        return null;
    }

    private static KtFunction getKtFunFromPsiReference(PsiReference reference) {
        if (!reference.getElement().getLanguage().is(Language.findLanguageByID("kotlin"))) {
            return null;
        }

        PsiElement element = reference.getElement();
        PsiElement userType = element.getParent();
        if (!(userType instanceof KtUserType)) {
            return null;
        }
        PsiElement ref = userType.getParent();

        PsiElement fun = ref.getParent();
        if (!(fun instanceof KtNamedFunction)) {
            return null;
        }
        return (KtNamedFunction) fun;
    }

    private static final GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS = (e, psiElement) -> {
        PsiElement clazz = psiElement.getParent();

        GlobalSearchScope allScope = ProjectScope.getProjectScope(clazz.getProject());
        Query<PsiReference> search = ReferencesSearch.search(clazz, allScope);

        List<KtFunction> functions = new ArrayList<>();
        for (PsiReference r : search) {
            KtFunction fun = getKtFunFromPsiReference(r);
            if (fun != null) {
                functions.add(fun);
            }
        }
        ExtMethodListPopup.show(functions, e);
    };

}
