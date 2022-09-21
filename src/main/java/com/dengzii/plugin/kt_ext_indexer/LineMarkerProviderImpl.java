package com.dengzii.plugin.kt_ext_indexer;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.stubindex.KotlinSourceFilterScope;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LineMarkerProviderImpl implements LineMarkerProvider {
    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (PsiElement element : elements) {
            if (!(element instanceof KtClassOrObject)) {
                continue;
            }

            if (element instanceof KtObjectDeclaration && ((KtObjectDeclaration)element).isCompanion()) {
                continue;
            }

            Project project = element.getProject();
            // ext. functions could be declared only in Kotlin sources
            // TODO: libraries search is out of scope so far
            GlobalSearchScope projectScope = KotlinSourceFilterScope.projectSources(GlobalSearchScope.projectScope(project), project);
            Query<PsiReference> search = ReferencesSearch.search(element, projectScope);
            // no reasons to search for all extension methods/properties to show that
            // there is at least one ext. function/properties
            PsiReference reference = search.findFirst();
            if (reference != null) {
                KtCallableDeclaration declaration = getKtDeclarationFromPsiReference(reference);
                if (declaration != null) {
                    PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
                    if (nameIdentifier == null) {
                        continue;
                    }
                    result.add(new LineMarkerInfo<>(nameIdentifier, nameIdentifier.getTextRange(), ShowExtIcon.icon, null, SHOW_RECEIVERS, GutterIconRenderer.Alignment.LEFT, () -> "extension methods"));
                }
            }
        }
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    private static KtCallableDeclaration getKtDeclarationFromPsiReference(PsiReference reference) {
        PsiElement element = reference.getElement();
        if (!(element instanceof KtElement)) {
            return null;
        }

        PsiElement userType = element.getParent();
        if (!(userType instanceof KtUserType)) {
            return null;
        }
        PsiElement ref = userType.getParent();

        PsiElement parent = ref.getParent();
        return (parent instanceof KtNamedFunction || parent instanceof KtProperty) ? (KtCallableDeclaration)parent : null;
    }

    private static final GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS = (e, psiElement) -> {
        PsiElement clazz = psiElement.getParent();

        GlobalSearchScope projectScope = ProjectScope.getProjectScope(clazz.getProject());
        Query<PsiReference> search = ReferencesSearch.search(clazz, projectScope);

        List<KtCallableDeclaration> declarations = new ArrayList<>();
        for (PsiReference reference : search) {
            KtCallableDeclaration declaration = getKtDeclarationFromPsiReference(reference);
            if (declaration != null) {
                declarations.add(declaration);
            }
        }
        ExtMethodListPopup.show(declarations, e);
    };

}
