package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtNamedFunction;

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
                return new LineMarkerInfo<>(nameIdentifier, nameIdentifier.getTextRange(), ShowExtIcon.icon,
                        null, SHOW_RECEIVERS, GutterIconRenderer.Alignment.LEFT, () -> "extension methods");
            }
        }
        return null;
    }

    private static KtFunction getKtFunFromPsiReference(PsiReference reference) {
        if (!reference.getElement().getLanguage().is(Language.findLanguageByID("kotlin"))) {
            return null;
        }

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

    private static final GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS = (e, psiElement) -> {
        KtClass ktClass = ((KtClass) psiElement.getParent());

        GlobalSearchScope allScope = ProjectScope.getProjectScope(ktClass.getProject());
        Query<PsiReference> search = ReferencesSearch.search(ktClass, allScope);

        List<KtFunction> functions = new ArrayList<>();
        for (PsiReference r : search) {
            KtFunction fun = getKtFunFromPsiReference(r);
            if (fun != null) {
                functions.add(fun);
            }
        }
        System.out.println("LineMarkerProviderImpl.show");
        ExtMethodListPopup.show(functions, e);
    };

}
